package com.june.controller.api;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/learn")
@CrossOrigin(origins = "*")
public class LearningController {

    private static final Path HIST_DIR = Paths.get("data", "history");
    private static final Path LEARN_DIR = Paths.get("data", "learn");
    private static final Path MODEL_FILE = LEARN_DIR.resolve("energy-model.json");

    private static final int MAX_LINES = 20000; // safety cap

    public LearningController(){
        try { Files.createDirectories(LEARN_DIR); } catch (Exception ignored) {}
    }

    @PostMapping(value = "/train", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,Object>> train(){
        try {
            Model m = fitModelFromHistory();
            writeModel(m);
            return okJson(m.toMap());
        } catch (Exception e){
            return err(500, "Training fehlgeschlagen: "+ e.getMessage());
        }
    }

    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,Object>> status(){
        try {
            Model m = readModel();
            return okJson(m != null ? m.toMap() : Map.of("trained", false));
        } catch (Exception e){
            return err(500, "Status-Fehler: "+e.getMessage());
        }
    }

    @GetMapping(value = "/predict", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,Object>> predict(@RequestParam(value = "steps", required = false) Integer steps){
        int n = (steps == null || steps < 1 || steps > 200) ? 10 : steps;
        try {
            Model m = readModel();
            if (m == null) return err(409, "Kein Modell trainiert");
            List<Double> preds = new ArrayList<>();
            for (int i=1;i<=n;i++) preds.add(m.predictNext(i));
            return okJson(Map.of(
                    "trainedTs", m.trainedTs,
                    "steps", n,
                    "predictions", preds
            ));
        } catch (Exception e){
            return err(500, "Vorhersage-Fehler: "+e.getMessage());
        }
    }

    // --- minimal linear model over energy values ---
    private Model fitModelFromHistory() throws Exception {
        List<Path> files = listHistoryFiles();
        long total = 0L;
        List<Double> y = new ArrayList<>();
        for (Path f : files){
            List<String> lines = Files.readAllLines(f, StandardCharsets.UTF_8);
            for (String line : lines){
                if (y.size() >= MAX_LINES) break;
                double v = extractEnergy(line);
                if (!Double.isNaN(v)) {
                    y.add(v);
                }
            }
            if (y.size() >= MAX_LINES) break;
        }
        total = y.size();
        if (total < 4) throw new IllegalStateException("Zu wenige Daten");
        // simple linear regression y ~ a + b*t
        double sumX=0, sumY=0, sumXX=0, sumXY=0;
        for (int i=0;i<y.size();i++){
            double xi = i+1; double yi = y.get(i);
            sumX += xi; sumY += yi; sumXX += xi*xi; sumXY += xi*yi;
        }
        double n = y.size();
        double denom = (n*sumXX - sumX*sumX);
        double b = denom != 0 ? (n*sumXY - sumX*sumY)/denom : 0.0; // slope
        double a = (sumY - b*sumX)/n; // intercept
        double min = y.stream().mapToDouble(d->d).min().orElse(0);
        double max = y.stream().mapToDouble(d->d).max().orElse(0);
        double avg = y.stream().mapToDouble(d->d).average().orElse(0);
        Model m = new Model();
        m.trainedTs = System.currentTimeMillis();
        m.n = (long) n;
        m.slope = b;
        m.intercept = a;
        m.min = min; m.max = max; m.avg = avg;
        return m;
    }

    private static List<Path> listHistoryFiles() throws Exception {
        List<Path> list = new ArrayList<>();
        if (Files.exists(HIST_DIR)){
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(HIST_DIR, "telemetry-*.jsonl")){
                for (Path p : ds) list.add(p);
            }
        }
        // newest first by lastModified
        list.sort((a,b)->{
            try{
                long ta = Files.getLastModifiedTime(a).toMillis();
                long tb = Files.getLastModifiedTime(b).toMillis();
                return Long.compare(tb, ta);
            }catch(Exception e){return 0;}
        });
        return list;
    }

    private static double extractEnergy(String line){
        if (line == null) return Double.NaN;
        int mIdx = line.indexOf("\"metrics\":");
        if (mIdx < 0) return Double.NaN;
        int eIdx = line.indexOf("\"energy\":", mIdx);
        if (eIdx < 0) return Double.NaN;
        int eStart = eIdx + 9;
        int i = eStart;
        StringBuilder num = new StringBuilder();
        while (i < line.length()){
            char c = line.charAt(i);
            if ((c>='0'&&c<='9') || c=='.' || c=='-') num.append(c); else break;
            i++;
        }
        try { return Double.parseDouble(num.toString()); } catch(Exception e){ return Double.NaN; }
    }

    private void writeModel(Model m) throws Exception {
        String json = toJson(m.toMap());
        Files.writeString(MODEL_FILE, json, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private Model readModel() throws Exception {
        if (!Files.exists(MODEL_FILE)) return null;
        // very small parser: we only need fields back; use Properties-like parse
        String s = Files.readString(MODEL_FILE, StandardCharsets.UTF_8);
        Model m = new Model();
        // naive extractions
        m.trainedTs = extractLong(s, "trainedTs");
        m.n = extractLong(s, "n");
        m.slope = extractDouble(s, "slope");
        m.intercept = extractDouble(s, "intercept");
        m.min = extractDouble(s, "min");
        m.max = extractDouble(s, "max");
        m.avg = extractDouble(s, "avg");
        return m;
    }

    private static long extractLong(String s, String key){
        int i = s.indexOf('"'+key+'"'); if (i<0) return 0;
        int c = s.indexOf(':', i); if (c<0) return 0;
        int j = c+1; StringBuilder b = new StringBuilder();
        while (j<s.length()){
            char ch = s.charAt(j++);
            if ((ch>='0'&&ch<='9')) b.append(ch); else if (b.length()>0) break;
        }
        try { return Long.parseLong(b.toString()); } catch(Exception e){ return 0; }
    }
    private static double extractDouble(String s, String key){
        int i = s.indexOf('"'+key+'"'); if (i<0) return 0;
        int c = s.indexOf(':', i); if (c<0) return 0;
        int j = c+1; StringBuilder b = new StringBuilder();
        while (j<s.length()){
            char ch = s.charAt(j++);
            if ((ch>='0'&&ch<='9')||ch=='.'||ch=='-') b.append(ch); else if (b.length()>0) break;
        }
        try { return Double.parseDouble(b.toString()); } catch(Exception e){ return 0; }
    }

    private static String toJson(Map<String,Object> m){
        StringBuilder sb = new StringBuilder();
        appendJson(sb, m);
        return sb.toString();
    }
    private static void appendJson(StringBuilder sb, Object obj){
        if (obj == null){ sb.append("null"); return; }
        if (obj instanceof Number || obj instanceof Boolean){ sb.append(String.valueOf(obj)); return; }
        if (obj instanceof String){ sb.append('"').append(escape((String)obj)).append('"'); return; }
        if (obj instanceof Map){ sb.append('{'); boolean first=true; for (Map.Entry<?,?> e: ((Map<?,?>)obj).entrySet()){ if(!first) sb.append(','); first=false; sb.append('"').append(escape(String.valueOf(e.getKey()))).append('"').append(':'); appendJson(sb,e.getValue()); } sb.append('}'); return; }
        if (obj instanceof Iterable){ sb.append('['); boolean first=true; for (Object v: (Iterable<?>)obj){ if(!first) sb.append(','); first=false; appendJson(sb,v);} sb.append(']'); return; }
        sb.append('"').append(escape(String.valueOf(obj))).append('"');
    }
    private static String escape(String s){ return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n"); }

    private static ResponseEntity<Map<String,Object>> okJson(Map<String,Object> b){
        return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store").body(b);
    }
    private static ResponseEntity<Map<String,Object>> err(int code, String msg){
        return ResponseEntity.status(code).contentType(MediaType.APPLICATION_JSON).body(Map.of("error", msg));
    }

    private static final class Model{
        long trainedTs;
        long n;
        double slope;
        double intercept;
        double min, max, avg;
        Map<String,Object> toMap(){
            return new LinkedHashMap<>(Map.of(
                    "trained", true,
                    "trainedTs", trainedTs,
                    "n", n,
                    "slope", slope,
                    "intercept", intercept,
                    "min", min,
                    "max", max,
                    "avg", avg
            ));
        }
        double predictNext(int step){
            double x = (n + step);
            return intercept + slope * x;
        }
    }
}
