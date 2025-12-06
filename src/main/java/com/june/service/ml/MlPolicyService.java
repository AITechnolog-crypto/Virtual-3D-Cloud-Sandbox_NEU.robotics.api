package com.june.service.ml;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Very small ML policy and dataset service.
 * - Stores policy in data/ml/policy.json
 * - Appends dataset rows to data/ml/dataset.jsonl
 * - Provides simple in-memory rate limiting for training
 */
@Service
public class MlPolicyService {
    private static final Path ML_DIR = Paths.get("data", "ml");
    private static final Path POLICY_FILE = ML_DIR.resolve("policy.json");
    private static final Path DATASET_FILE = ML_DIR.resolve("dataset.jsonl");

    public MlPolicyService(){
        try { Files.createDirectories(ML_DIR); } catch (Exception ignored) {}
    }

    public static final class Policy {
        public boolean trainingEnabled = true;
        public boolean ingestEnabled = true;
        public boolean mirrorTelemetryToDataset = false;
        public int retentionDays = 30; // dataset retention advice (not enforced here)
        public int maxTrainPerHour = 6;
        public boolean allowExternalModels = false;
        public Map<String,Object> extra = new LinkedHashMap<>();
    }

    public Policy load(){
        if (!Files.exists(POLICY_FILE)) return new Policy();
        try {
            String s = Files.readString(POLICY_FILE, StandardCharsets.UTF_8);
            return fromJson(s);
        } catch (Exception e){
            return new Policy();
        }
    }

    public void save(Policy p){
        try {
            String json = toJson(p);
            Files.writeString(POLICY_FILE, json, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ignored) {}
    }

    public void appendDatasetRow(Map<String,Object> row) throws IOException {
        if (row == null) return;
        Map<String,Object> safe = new LinkedHashMap<>(row);
        safe.putIfAbsent("ts", System.currentTimeMillis());
        String line = toJson(safe) + "\n";
        Files.writeString(DATASET_FILE, line, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    // --- simple rate limiter for training ---
    private final Map<String, Deque<Long>> trainWindows = new ConcurrentHashMap<>();

    public boolean canTrain(String clientId, int maxPerHour){
        String key = clientId == null ? "default" : clientId;
        long now = System.currentTimeMillis();
        long cutoff = now - 3600_000L;
        Deque<Long> q = trainWindows.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (q){
            while (!q.isEmpty() && q.peekFirst() < cutoff) q.pollFirst();
            return q.size() < Math.max(1, maxPerHour);
        }
    }
    public void recordTrain(String clientId){
        String key = clientId == null ? "default" : clientId;
        Deque<Long> q = trainWindows.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (q){ q.addLast(System.currentTimeMillis()); }
    }

    // --- JSON helpers ---
    private static Policy fromJson(String s){
        Policy p = new Policy();
        try {
            // very small/naive parsing by key presence
            p.trainingEnabled = getBool(s, "trainingEnabled", true);
            p.ingestEnabled = getBool(s, "ingestEnabled", true);
            p.mirrorTelemetryToDataset = getBool(s, "mirrorTelemetryToDataset", false);
            p.retentionDays = (int)getLong(s, "retentionDays", 30);
            p.maxTrainPerHour = (int)getLong(s, "maxTrainPerHour", 6);
            p.allowExternalModels = getBool(s, "allowExternalModels", false);
        } catch (Exception ignored) {}
        return p;
    }
    private static boolean getBool(String s, String key, boolean def){
        int i = s.indexOf('"'+key+'"'); if (i<0) return def; int c = s.indexOf(':', i); if (c<0) return def;
        int j=c+1; while (j<s.length() && Character.isWhitespace(s.charAt(j))) j++;
        if (s.startsWith("true", j)) return true; if (s.startsWith("false", j)) return false; return def;
    }
    private static long getLong(String s, String key, long def){
        int i = s.indexOf('"'+key+'"'); if (i<0) return def; int c = s.indexOf(':', i); if (c<0) return def;
        int j=c+1; StringBuilder b=new StringBuilder();
        while (j<s.length()){
            char ch = s.charAt(j++);
            if ((ch>='0'&&ch<='9')) b.append(ch); else if (b.length()>0) break;
        }
        try { return Long.parseLong(b.toString()); } catch (Exception e){ return def; }
    }

    private static String toJson(Policy p){
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("trainingEnabled", p.trainingEnabled);
        m.put("ingestEnabled", p.ingestEnabled);
        m.put("mirrorTelemetryToDataset", p.mirrorTelemetryToDataset);
        m.put("retentionDays", p.retentionDays);
        m.put("maxTrainPerHour", p.maxTrainPerHour);
        m.put("allowExternalModels", p.allowExternalModels);
        if (p.extra != null && !p.extra.isEmpty()) m.put("extra", p.extra);
        return toJson(m);
    }

    private static String toJson(Map<String,Object> m){
        StringBuilder sb = new StringBuilder();
        appendJson(sb, m); return sb.toString();
    }
    @SuppressWarnings("unchecked")
    private static void appendJson(StringBuilder sb, Object obj){
        if (obj == null){ sb.append("null"); return; }
        if (obj instanceof Number || obj instanceof Boolean){ sb.append(String.valueOf(obj)); return; }
        if (obj instanceof String){ sb.append('"').append(escape((String)obj)).append('"'); return; }
        if (obj instanceof Map){ sb.append('{'); boolean first=true; for (Map.Entry<?,?> e: ((Map<?,?>)obj).entrySet()){ if(!first) sb.append(','); first=false; sb.append('"').append(escape(String.valueOf(e.getKey()))).append('"').append(':'); appendJson(sb,e.getValue()); } sb.append('}'); return; }
        if (obj instanceof Iterable){ sb.append('['); boolean first=true; for (Object v: (Iterable<?>)obj){ if(!first) sb.append(','); first=false; appendJson(sb,v);} sb.append(']'); return; }
        sb.append('"').append(escape(String.valueOf(obj))).append('"');
    }
    private static String escape(String s){ return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n"); }
}
