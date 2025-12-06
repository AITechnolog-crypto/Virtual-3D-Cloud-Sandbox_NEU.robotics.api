package com.june.service.history;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Very small file-based append-only history store (JSONL files).
 * - Writes telemetry snapshots to data/history/telemetry-YYYY-MM.jsonl
 * - Provides simple iteration to compute summaries without loading entire file into memory at once.
 */
@Component
public class HistoryStore {
    private static final Path DATA_DIR = Paths.get("data", "history");

    public HistoryStore(){
        try { Files.createDirectories(DATA_DIR); } catch (Exception ignored) {}
    }

    public void append(Map<String,Object> payload){
        try {
            Path file = currentFile();
            String line = toJsonLine(payload) + "\n";
            Files.writeString(file, line, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {}
    }

    public Summary summarize(int maxFiles){
        Summary s = new Summary();
        try {
            List<Path> files = listFilesSortedNewestFirst();
            int count = 0;
            for (Path f : files){
                if (maxFiles > 0 && count >= maxFiles) break;
                count++;
                summarizeFile(f, s);
            }
        } catch (Exception ignored) {}
        return s;
    }

    private List<Path> listFilesSortedNewestFirst() throws IOException {
        List<Path> list = new ArrayList<>();
        if (Files.exists(DATA_DIR)) {
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(DATA_DIR, "telemetry-*.jsonl")) {
                for (Path p : ds) list.add(p);
            }
        }
        list.sort((a,b) -> {
            try {
                long ta = Files.getLastModifiedTime(a).toMillis();
                long tb = Files.getLastModifiedTime(b).toMillis();
                return Long.compare(tb, ta);
            } catch (IOException e) { return 0; }
        });
        return list;
    }

    public static final class Summary{
        public long firstTs = Long.MAX_VALUE;
        public long lastTs = 0L;
        public long totalEvents = 0L;
        // simple metrics aggregation for common fields
        public double energyMin = Double.POSITIVE_INFINITY;
        public double energyMax = Double.NEGATIVE_INFINITY;
        public double energySum = 0.0;
        public long energyCount = 0L;

        public Map<String,Object> toMap(){
            Map<String,Object> m = new LinkedHashMap<>();
            if (firstTs == Long.MAX_VALUE) firstTs = 0L;
            m.put("firstTs", firstTs);
            m.put("lastTs", lastTs);
            m.put("totalEvents", totalEvents);
            if (energyCount > 0){
                m.put("energy", Map.of(
                        "min", energyMin == Double.POSITIVE_INFINITY ? null : energyMin,
                        "max", energyMax == Double.NEGATIVE_INFINITY ? null : energyMax,
                        "avg", energySum / Math.max(1, energyCount),
                        "count", energyCount
                ));
            } else {
                m.put("energy", Map.of("count", 0));
            }
            return m;
        }
    }

    private void summarizeFile(Path f, Summary s){
        try {
            List<String> lines = Files.readAllLines(f, StandardCharsets.UTF_8);
            for (String line : lines){
                if (line == null || line.isBlank()) continue;
                s.totalEvents++;
                // very naive parse: extract ts and metrics.energy if present
                try {
                    int tsIdx = line.indexOf("\"ts\":");
                    if (tsIdx >= 0){
                        int start = tsIdx + 5;
                        int end = findNumberEnd(line, start);
                        long ts = Long.parseLong(line.substring(start, end).replaceAll("[^0-9]",""));
                        if (s.firstTs == 0L || s.firstTs == Long.MAX_VALUE) s.firstTs = ts;
                        if (ts < s.firstTs) s.firstTs = ts;
                        if (ts > s.lastTs) s.lastTs = ts;
                    }
                    int mIdx = line.indexOf("\"metrics\":");
                    if (mIdx >= 0){
                        int eIdx = line.indexOf("\"energy\":", mIdx);
                        if (eIdx >= 0){
                            int eStart = eIdx + 9;
                            int eEnd = findNumberEnd(line, eStart);
                            String sub = line.substring(eStart, Math.min(line.length(), eEnd));
                            double v = parseNumberSafe(sub);
                            if (!Double.isNaN(v)) {
                                s.energyMin = Math.min(s.energyMin, v);
                                s.energyMax = Math.max(s.energyMax, v);
                                s.energySum += v;
                                s.energyCount++;
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }

    private static double parseNumberSafe(String s){
        if (s == null) return Double.NaN;
        StringBuilder b = new StringBuilder();
        for (int i=0;i<s.length();i++){
            char c = s.charAt(i);
            if ((c>='0'&&c<='9') || c=='.' || c=='-') b.append(c);
        }
        try { return Double.parseDouble(b.toString()); } catch (Exception e){ return Double.NaN; }
    }

    private static int findNumberEnd(String s, int start){
        int i = start;
        while (i < s.length()){
            char c = s.charAt(i);
            if ((c >= '0' && c <= '9') || c == '.' || c == '-') { i++; } else { break; }
        }
        return i;
    }

    private Path currentFile(){
        String ym = new SimpleDateFormat("yyyy-MM").format(new Date());
        return DATA_DIR.resolve("telemetry-"+ym+".jsonl");
    }

    @SuppressWarnings("unchecked")
    private static String toJsonLine(Map<String,Object> map){
        StringBuilder sb = new StringBuilder();
        appendJson(sb, map);
        return sb.toString();
    }

    private static void appendJson(StringBuilder sb, Object obj){
        if (obj == null){ sb.append("null"); return; }
        if (obj instanceof Number || obj instanceof Boolean){ sb.append(String.valueOf(obj)); return; }
        if (obj instanceof String){ sb.append('"').append(escape((String)obj)).append('"'); return; }
        if (obj instanceof Map){
            sb.append('{'); boolean first = true;
            for (Map.Entry<?,?> e : ((Map<?,?>)obj).entrySet()){
                if (!first) sb.append(','); first = false;
                sb.append('"').append(escape(String.valueOf(e.getKey()))).append('"').append(':');
                appendJson(sb, e.getValue());
            }
            sb.append('}'); return;
        }
        if (obj instanceof Iterable){
            sb.append('['); boolean first = true;
            for (Object v : (Iterable<?>)obj){
                if (!first) sb.append(','); first = false; appendJson(sb, v);
            }
            sb.append(']'); return;
        }
        sb.append('"').append(escape(String.valueOf(obj))).append('"');
    }

    private static String escape(String s){
        return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n");
    }
}
