package com.june.service.federation;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

/**
 * File-based federation store for sharing simple model parameters between instances.
 * - Appends submitted models to data/federation/models.jsonl
 * - Computes a simple average aggregate written to data/federation/aggregate-energy.json
 */
@Component
public class FederationStore {
    private static final Path FED_DIR = Paths.get("data", "federation");
    private static final Path MODELS_FILE = FED_DIR.resolve("models.jsonl");
    private static final Path AGG_FILE = FED_DIR.resolve("aggregate-energy.json");

    public FederationStore(){
        try { Files.createDirectories(FED_DIR); } catch (Exception ignored) {}
    }

    public synchronized void submit(Map<String, Object> model){
        Objects.requireNonNull(model, "model");
        Map<String,Object> copy = new LinkedHashMap<>(model);
        copy.putIfAbsent("ts", Instant.now().toEpochMilli());
        try {
            String json = toJson(copy) + "\n";
            Files.writeString(MODELS_FILE, json, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception ignored) {}
    }

    public synchronized Map<String,Object> aggregate(int max){
        List<Map<String,Object>> items = readLast(max);
        if (items.isEmpty()) return Map.of("count", 0);
        double slopeSum=0, interceptSum=0, min=Double.POSITIVE_INFINITY, maxV=Double.NEGATIVE_INFINITY, avgSum=0;
        int n=0;
        for (Map<String,Object> m : items){
            Double slope = asD(m.get("slope"));
            Double intercept = asD(m.get("intercept"));
            Double aMin = asD(m.get("min"));
            Double aMax = asD(m.get("max"));
            Double aAvg = asD(m.get("avg"));
            if (slope!=null && intercept!=null){
                slopeSum += slope; interceptSum += intercept; n++;
            }
            if (aMin!=null) min = Math.min(min, aMin);
            if (aMax!=null) maxV = Math.max(maxV, aMax);
            if (aAvg!=null) avgSum += aAvg;
        }
        Map<String,Object> agg = new LinkedHashMap<>();
        agg.put("ts", Instant.now().toEpochMilli());
        agg.put("count", items.size());
        if (n>0){
            agg.put("slope", slopeSum/n);
            agg.put("intercept", interceptSum/n);
        }
        if (min!=Double.POSITIVE_INFINITY) agg.put("min", min);
        if (maxV!=Double.NEGATIVE_INFINITY) agg.put("max", maxV);
        if (items.size()>0) agg.put("avg", avgSum/Math.max(1, items.size()));
        // persist
        try { Files.writeString(AGG_FILE, toJson(agg), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);} catch(Exception ignored) {}
        return agg;
    }

    public List<Map<String,Object>> list(int limit){
        return readLast(limit);
    }

    public static Path aggregateFile(){ return AGG_FILE; }

    // --- helpers ---
    private List<Map<String,Object>> readLast(int limit){
        List<Map<String,Object>> out = new ArrayList<>();
        try {
            if (!Files.exists(MODELS_FILE)) return out;
            List<String> lines = Files.readAllLines(MODELS_FILE, StandardCharsets.UTF_8);
            int start = Math.max(0, lines.size() - Math.max(1, limit));
            for (int i=start; i<lines.size(); i++){
                String line = lines.get(i);
                Map<String,Object> m = parseJsonLine(line);
                if (m!=null) out.add(m);
            }
        } catch (Exception ignored) {}
        return out;
    }

    private static Double asD(Object o){
        if (o == null) return null;
        if (o instanceof Number) return ((Number)o).doubleValue();
        try { return Double.parseDouble(String.valueOf(o)); } catch(Exception e){ return null; }
    }

    private static String toJson(Map<String,Object> map){
        StringBuilder sb = new StringBuilder(); appendJson(sb, map); return sb.toString();
    }
    private static void appendJson(StringBuilder sb, Object obj){
        if (obj == null){ sb.append("null"); return; }
        if (obj instanceof Number || obj instanceof Boolean){ sb.append(String.valueOf(obj)); return; }
        if (obj instanceof String){ sb.append('"').append(escape((String)obj)).append('"'); return; }
        if (obj instanceof Map){ sb.append('{'); boolean first=true; for (Map.Entry<?,?> e: ((Map<?,?>)obj).entrySet()){ if(!first) sb.append(','); first=false; sb.append('"').append(escape(String.valueOf(e.getKey()))).append('"').append(':'); appendJson(sb, e.getValue()); } sb.append('}'); return; }
        if (obj instanceof Iterable){ sb.append('['); boolean first=true; for (Object v: (Iterable<?>)obj){ if(!first) sb.append(','); first=false; appendJson(sb, v);} sb.append(']'); return; }
        sb.append('"').append(escape(String.valueOf(obj))).append('"');
    }
    private static String escape(String s){ return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n"); }

    @SuppressWarnings("unchecked")
    private static Map<String,Object> parseJsonLine(String line){
        if (line == null || line.isBlank()) return null;
        // very small tolerant parser: expect key:value pairs at top-level
        Map<String,Object> m = new LinkedHashMap<>();
        try {
            String s = line.trim();
            if (s.startsWith("{") && s.endsWith("}")) s = s.substring(1, s.length()-1);
            String[] parts = s.split(",");
            for (String p : parts){
                int i = p.indexOf(':'); if (i<=0) continue;
                String k = p.substring(0,i).trim().replaceAll("^\"|\"$", "");
                String v = p.substring(i+1).trim();
                if (v.startsWith("\"") && v.endsWith("\"")) { m.put(k, v.substring(1, v.length()-1)); }
                else if (v.equals("null")) { m.put(k, null); }
                else if (v.equals("true")||v.equals("false")) { m.put(k, Boolean.valueOf(v)); }
                else { try { m.put(k, Double.parseDouble(v.replaceAll("[^0-9eE+\\-\\.]",""))); } catch(Exception e){ m.put(k, v); } }
            }
        } catch (Exception ignored) {}
        return m;
    }
}
