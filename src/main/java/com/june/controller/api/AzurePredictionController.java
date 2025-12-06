package com.june.controller.api;

import com.june.service.external.AzurePredictionClient;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/azure")
@CrossOrigin(origins = "*")
public class AzurePredictionController {

    private final Environment env;
    private static final int MAX_JSON_CHARS = 30_000;
    private static final Set<String> BLOCKLIST = Set.of("<script>", "</script>", "..\\", "../", "\u0000");

    // naive per-IP rate limit
    private static final Map<String, Deque<Long>> RATE = new ConcurrentHashMap<>();
    private static final int LIMIT_PER_MIN = 30;

    public AzurePredictionController(Environment env){
        this.env = env;
    }

    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String,Object>> status(){
        AzurePredictionClient client = new AzurePredictionClient(env);
        Map<String,Object> out = new LinkedHashMap<>();
        out.put("configured", client.configured());
        try {
            java.net.URI u = java.net.URI.create(getEndpointSafe());
            String host = Optional.ofNullable(u.getHost()).orElse("");
            out.put("endpointHost", host);
        } catch (Exception e){
            out.put("endpointHost", "");
        }
        out.put("region", getRegionSafe());
        out.put("time", Instant.now().toString());
        return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store").body(out);
    }

    @PostMapping(value = "/predict", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> predict(@RequestBody(required = false) String json,
                                          @RequestHeader Map<String,String> headers,
                                          @RequestParam(value = "path", required = false) String relativePath){
        String clientIp = Optional.ofNullable(headers.get("x-forwarded-for")).orElse("direct");
        if (!consumeRate(clientIp)){
            return ResponseEntity.status(429).contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"Rate Limit: max "+LIMIT_PER_MIN+"/Minute\"}");
        }
        if (json == null) json = "{}";
        if (json.length() > MAX_JSON_CHARS){
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"Payload zu groß\"}");
        }
        String lower = json.toLowerCase(Locale.ROOT);
        for (String bad : BLOCKLIST){
            if (lower.contains(bad.toLowerCase(Locale.ROOT))){
                return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"Payload durch Sicherheitsfilter blockiert\"}");
            }
        }
        AzurePredictionClient client = new AzurePredictionClient(env);
        if (!client.configured()){
            return ResponseEntity.status(503)
                    .header(HttpHeaders.CACHE_CONTROL, "no-store")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"Azure Prediction nicht konfiguriert oder unsicherer Endpoint. Bitte AZURE_PREDICTION_ENDPOINT (*.azure.com/.net), AZURE_PREDICTION_KEY setzen.\"}");
        }
        try{
            String body = client.postJson(relativePath, json);
            String wrapped = "{"+
                    "\"ts\":"+ System.currentTimeMillis() +","+
                    "\"ok\":true,"+
                    "\"response\":" + safeEmbed(body) +
                    "}";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, "no-store")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(wrapped);
        }catch(Exception e){
            String msg = e.getMessage();
            if (msg == null) msg = "Fehler";
            return ResponseEntity.status(502).contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"Azure Anfrage fehlgeschlagen: "+ escape(msg) +"\"}");
        }
    }

    private static boolean consumeRate(String ip){
        long now = Instant.now().toEpochMilli();
        Deque<Long> q = RATE.computeIfAbsent(ip, k -> new ArrayDeque<>());
        synchronized (q){
            long cutoff = now - 60_000L;
            while (!q.isEmpty() && q.peekFirst() < cutoff) q.pollFirst();
            if (q.size() >= LIMIT_PER_MIN) return false;
            q.addLast(now);
            return true;
        }
    }

    private static String escape(String s){
        return s.replace("\\", "\\\\").replace("\"","\\\"").replace("\n","\\n");
    }
    // embed external JSON as string value safely (best-effort)
    private static String safeEmbed(String raw){
        if (raw == null) return "{}";
        return '"' + escape(raw) + '"';
    }

    private String getEndpointSafe(){
        try{ String val = Optional.ofNullable(System.getenv("AZURE_PREDICTION_ENDPOINT")).orElse(""); if (val==null||val.isBlank()) val = env!=null? env.getProperty("azure.prediction.endpoint", ""):""; return val; }catch(Exception e){ return ""; }
    }
    private String getRegionSafe(){
        try{ String val = Optional.ofNullable(System.getenv("AZURE_REGION")).orElse(""); if (val==null||val.isBlank()) val = env!=null? env.getProperty("azure.region", "eastus"):"eastus"; return val; }catch(Exception e){ return "eastus"; }
    }
}
