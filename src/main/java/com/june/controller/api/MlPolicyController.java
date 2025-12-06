package com.june.controller.api;

import com.june.controller.api.LearningController;
import com.june.service.ml.MlPolicyService;
import com.june.service.ml.MlPolicyService.Policy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/api/ml")
@CrossOrigin(origins = "*")
public class MlPolicyController {

    private final MlPolicyService policySvc;
    private final LearningController learning;

    private static final int MAX_PAYLOAD_CHARS = 20_000;
    private static final Set<String> BLOCKLIST = Set.of("<script>", "</script>", "\u0000");

    public MlPolicyController(MlPolicyService policySvc, LearningController learning) {
        this.policySvc = policySvc;
        this.learning = learning; // delegate to existing learning endpoints
    }

    @GetMapping(value = "/policy", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,Object>> getPolicy(){
        Policy p = policySvc.load();
        return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store").body(toMap(p));
    }

    @PostMapping(value = "/policy", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,Object>> updatePolicy(@RequestBody Map<String,Object> body){
        Policy p = policySvc.load();
        try{
            if (body.containsKey("trainingEnabled")) p.trainingEnabled = asBool(body.get("trainingEnabled"), p.trainingEnabled);
            if (body.containsKey("ingestEnabled")) p.ingestEnabled = asBool(body.get("ingestEnabled"), p.ingestEnabled);
            if (body.containsKey("mirrorTelemetryToDataset")) p.mirrorTelemetryToDataset = asBool(body.get("mirrorTelemetryToDataset"), p.mirrorTelemetryToDataset);
            if (body.containsKey("retentionDays")) p.retentionDays = clampInt(body.get("retentionDays"), 1, 365, p.retentionDays);
            if (body.containsKey("maxTrainPerHour")) p.maxTrainPerHour = clampInt(body.get("maxTrainPerHour"), 1, 60, p.maxTrainPerHour);
            if (body.containsKey("allowExternalModels")) p.allowExternalModels = asBool(body.get("allowExternalModels"), p.allowExternalModels);
            policySvc.save(p);
            return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store").body(toMap(p));
        }catch(Exception e){
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(Map.of("error", "Ungültige Policy: "+e.getMessage()));
        }
    }

    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,Object>> getStatus(){
        Map<String,Object> m = new LinkedHashMap<>();
        Policy p = policySvc.load();
        m.put("policy", toMap(p));
        // Include model status if exists
        try{
            Path model = Paths.get("data", "learn", "energy-model.json");
            if (Files.exists(model)){
                String s = Files.readString(model, StandardCharsets.UTF_8);
                long ts = extractLong(s, "trainedTs");
                long n = extractLong(s, "n");
                m.put("model", Map.of("trained", true, "trainedTs", ts, "n", n));
            } else {
                m.put("model", Map.of("trained", false));
            }
        }catch(Exception ignored){}
        return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store").body(m);
    }

    public static class IngestRow {
        public Map<String,Object> features;
        public String label;
        public String source;
    }

    @PostMapping(value = "/ingest", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,Object>> ingest(@RequestBody IngestRow row, @RequestHeader Map<String,String> headers){
        Policy p = policySvc.load();
        if (!p.ingestEnabled){
            return ResponseEntity.status(409).contentType(MediaType.APPLICATION_JSON).body(Map.of("error", "Ingest deaktiviert durch Policy"));
        }
        String raw = String.valueOf(row);
        if (raw.length() > MAX_PAYLOAD_CHARS){
            return ResponseEntity.badRequest().body(Map.of("error", "Payload zu groß"));
        }
        String s = raw.toLowerCase(Locale.ROOT);
        for (String bad : BLOCKLIST){
            if (s.contains(bad.toLowerCase(Locale.ROOT))){
                return ResponseEntity.badRequest().body(Map.of("error", "Payload durch Sicherheitsfilter blockiert"));
            }
        }
        try{
            Map<String,Object> payload = new LinkedHashMap<>();
            payload.put("features", row.features == null ? Map.of() : row.features);
            if (row.label != null) payload.put("label", row.label);
            if (row.source != null) payload.put("source", row.source);
            payload.put("ts", System.currentTimeMillis());
            policySvc.appendDatasetRow(payload);
            return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store").body(Map.of("ok", true, "rows", 1));
        }catch(Exception e){
            return ResponseEntity.status(500).contentType(MediaType.APPLICATION_JSON).body(Map.of("error", "Ingest-Fehler: "+e.getMessage()));
        }
    }

    // Delegation with policy checks
    @PostMapping(value = "/train", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,Object>> train(@RequestHeader(value = "X-ORG-ID", required = false) String org){
        Policy p = policySvc.load();
        if (!p.trainingEnabled){
            return ResponseEntity.status(409).contentType(MediaType.APPLICATION_JSON).body(Map.of("error", "Training deaktiviert durch Policy"));
        }
        if (!policySvc.canTrain(org, p.maxTrainPerHour)){
            return ResponseEntity.status(429).contentType(MediaType.APPLICATION_JSON).body(Map.of("error", "Rate Limit: max "+p.maxTrainPerHour+"/h"));
        }
        // delegate to LearningController
        ResponseEntity<Map<String,Object>> res = learning.train();
        if (res.getStatusCode().is2xxSuccessful()) policySvc.recordTrain(org);
        return res;
    }

    @GetMapping(value = "/predict", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,Object>> predict(@RequestParam(value = "steps", required = false) Integer steps){
        return learning.predict(steps);
    }

    // --- helpers ---
    private static Map<String,Object> toMap(Policy p){
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("trainingEnabled", p.trainingEnabled);
        m.put("ingestEnabled", p.ingestEnabled);
        m.put("mirrorTelemetryToDataset", p.mirrorTelemetryToDataset);
        m.put("retentionDays", p.retentionDays);
        m.put("maxTrainPerHour", p.maxTrainPerHour);
        m.put("allowExternalModels", p.allowExternalModels);
        return m;
    }
    private static boolean asBool(Object o, boolean def){ if (o==null) return def; if (o instanceof Boolean) return (Boolean)o; return "true".equalsIgnoreCase(String.valueOf(o)); }
    private static int clampInt(Object o, int min, int max, int def){
        try{ int v = Integer.parseInt(String.valueOf(o)); return Math.max(min, Math.min(max, v)); }catch(Exception e){ return def; }
    }
    private static long extractLong(String s, String key){
        int i = s.indexOf('"'+key+'"'); if (i<0) return 0; int c = s.indexOf(':', i); if (c<0) return 0; int j=c+1; StringBuilder b=new StringBuilder();
        while (j<s.length()){ char ch = s.charAt(j++); if ((ch>='0'&&ch<='9')) b.append(ch); else if (b.length()>0) break; }
        try { return Long.parseLong(b.toString()); } catch(Exception e){ return 0; }
    }
}
