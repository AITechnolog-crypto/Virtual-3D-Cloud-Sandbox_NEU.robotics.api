package com.june.controller.api;

import com.june.service.external.DeepSeekClient;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/deepseek")
@CrossOrigin(origins = "*")
public class DeepSeekApiController {

    private final Environment env;
    private static final int MAX_PROMPT_CHARS = 4000; // server-side cap
    private static final Set<String> BLOCKLIST = Set.of(
            "DROP TABLE", "rm -rf", "shutdown", "format c:", "dd if=", "\u0000"
    );

    // naive in-memory rate limiter (per-IP)
    private static final Map<String, Deque<Long>> RATE = new ConcurrentHashMap<>();
    private static final int LIMIT_PER_MIN = 20; // 20 requests per minute per IP

    public DeepSeekApiController(Environment env) {
        this.env = env;
    }

    public static class ChatRequest {
        public String prompt;
        public String model; // optional
        public Integer maxTokens; // optional
        public Double temperature; // optional
    }

    @PostMapping(value = "/chat", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> chat(@RequestBody ChatRequest body, @RequestHeader Map<String, String> headers) {
        String clientIp = Optional.ofNullable(headers.get("x-forwarded-for")).orElse("direct");
        if (!consumeRate(clientIp)) {
            return json(429, Map.of("error", "Rate Limit: max "+LIMIT_PER_MIN+"/Minute"));
        }

        if (body == null || body.prompt == null || body.prompt.isBlank()) {
            return json(400, Map.of("error", "prompt fehlt"));
        }
        String prompt = body.prompt.trim();
        if (prompt.length() > MAX_PROMPT_CHARS) {
            prompt = prompt.substring(0, MAX_PROMPT_CHARS);
        }
        // simple abuse filter
        for (String bad : BLOCKLIST) {
            if (prompt.toLowerCase(Locale.ROOT).contains(bad.toLowerCase(Locale.ROOT))) {
                return json(400, Map.of("error", "Inhalt blockiert durch Sicherheitsfilter"));
            }
        }

        DeepSeekClient client = new DeepSeekClient(env);
        if (!client.hasKey()) {
            return ResponseEntity.status(503)
                    .header(HttpHeaders.CACHE_CONTROL, "no-store")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"DEEPSEEK_API_KEY fehlt. Bitte als Umgebungsvariable oder deepseek.api.key setzen.\"}");
        }
        try {
            String json = client.chat(body.model, prompt, body.maxTokens, body.temperature);
            // minimal audit envelope: timestamp and sanitized length
            String wrapped = "{"+
                    "\"ts\":"+System.currentTimeMillis()+","+
                    "\"ok\":true,"+
                    "\"response\":"+json+"}";
            // Note: json is itself a JSON string from API; for simplicity we embed directly
            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, "no-store")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(wrapped);
        } catch (Exception e) {
            return json(502, Map.of("error", "DeepSeek Anfrage fehlgeschlagen: "+ e.getMessage()));
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

    private static ResponseEntity<String> json(int status, Map<String, Object> map){
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean first = true;
        for (Map.Entry<String,Object> e : map.entrySet()){
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(escape(e.getKey())).append('"').append(':');
            Object v = e.getValue();
            if (v == null){ sb.append("null"); }
            else if (v instanceof Number || v instanceof Boolean){ sb.append(v.toString()); }
            else { sb.append('"').append(escape(String.valueOf(v))).append('"'); }
        }
        sb.append('}');
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(sb.toString());
    }

    private static String escape(String s){
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
