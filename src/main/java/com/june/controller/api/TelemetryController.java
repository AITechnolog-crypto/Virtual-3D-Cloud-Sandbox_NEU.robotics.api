package com.june.controller.api;

import com.june.service.history.HistoryStore;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Lightweight telemetry endpoints to integrate future Java simulations in real time.
 * - POST /api/telemetry/push  → accepts a small JSON payload and stores it in-memory
 * - GET  /api/telemetry/status → returns latest snapshot
 * - GET  /api/telemetry/stream → Server-Sent Events emitting on updates
 */
@RestController
@RequestMapping("/api/telemetry")
@CrossOrigin(origins = "*")
public class TelemetryController {
 
    private static final int MAX_JSON_CHARS = 20_000;
    private static final Set<String> BLOCKLIST = Set.of("<script>", "</script>", "\u0000");
 
    private volatile Map<String, Object> latest = baseSnapshot();
    private final List<SseEmitter> listeners = new CopyOnWriteArrayList<>();
    private final HistoryStore history;
 
    public TelemetryController(HistoryStore history){
        this.history = history;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status(){
        return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store").body(latest);
    }

    @PostMapping(value = "/push", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> push(@RequestBody Map<String, Object> payload){
        String jsonLenCalc = String.valueOf(payload);
        if (jsonLenCalc.length() > MAX_JSON_CHARS){
            return ResponseEntity.badRequest().body(Map.of("error", "Payload zu groß"));
        }
        String asString = jsonLenCalc.toLowerCase(Locale.ROOT);
        for (String bad : BLOCKLIST){
            if (asString.contains(bad.toLowerCase(Locale.ROOT))){
                return ResponseEntity.badRequest().body(Map.of("error", "Payload durch Sicherheitsfilter blockiert"));
            }
        }
        Map<String, Object> snap = new LinkedHashMap<>(baseSnapshot());
        snap.put("ts", System.currentTimeMillis());
        snap.put("source", payload.getOrDefault("source", "unknown"));
        Object metrics = payload.get("metrics");
        if (metrics instanceof Map){
            // merge selected metrics into a flat view for convenience
            @SuppressWarnings("unchecked") Map<String,Object> mm = (Map<String,Object>) metrics;
            snap.put("metrics", new LinkedHashMap<>(mm));
        } else {
            snap.put("metrics", Map.of());
        }
        this.latest = snap;
        // Persist to history (best-effort)
        try { if (history != null) history.append(snap); } catch (Exception ignored) {}
        emitSse(snap);
        return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store").body(snap);
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(){
        SseEmitter emitter = new SseEmitter(0L); // no timeout
        listeners.add(emitter);
        emitter.onCompletion(() -> listeners.remove(emitter));
        emitter.onTimeout(() -> listeners.remove(emitter));
        // send initial snapshot
        try {
            emitter.send(SseEmitter.event().name("snapshot").data(latest));
        } catch (IOException ignored) {}
        return emitter;
    }

    private void emitSse(Map<String, Object> data){
        for (SseEmitter e : listeners){
            try {
                e.send(SseEmitter.event().name("update").data(data));
            } catch (Exception ex){
                try { e.complete(); } catch (Exception ignore) {}
                listeners.remove(e);
            }
        }
    }

    private static Map<String, Object> baseSnapshot(){
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("ts", Instant.now().toEpochMilli());
        m.put("source", "init");
        m.put("metrics", Map.of(
                "energy", 2847,
                "threats", 0,
                "temperature", 23
        ));
        return m;
    }
}
