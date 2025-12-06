package com.june.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * ActionsController – kleine Simulations-Endpoints für die UI-Buttons.
 * Ziel: Keine 404 mehr, sichtbare Live-Aktualisierung via TelemetryController.
 *
 * Endpunkte (POST):
 *  - /api/robots/deploy
 *  - /api/energy/collect
 *  - /api/threats/simulate
 *  - /api/seed
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ActionsController {

    private final TelemetryController telemetry;

    public ActionsController(TelemetryController telemetry) {
        this.telemetry = telemetry;
    }

    @PostMapping("/robots/deploy")
    public Map<String, Object> deployRobots() {
        Map<String, Object> metrics = currentMetrics();
        // kleiner positiver Effekt auf die Energie
        double energy = asDouble(metrics.get("energy"));
        metrics.put("energy", Math.max(0, energy + 50));
        pushTelemetry("action:robots/deploy", metrics);
        return Map.of("ok", true, "deployed", 100);
    }

    @PostMapping("/energy/collect")
    public Map<String, Object> collectEnergy() {
        Map<String, Object> metrics = currentMetrics();
        // deutlichere Energie-Erhöhung (simulativ)
        double energy = asDouble(metrics.get("energy"));
        double delta = 200 + Math.random() * 300; // 200..500
        metrics.put("energy", Math.max(0, energy + Math.round(delta)));
        pushTelemetry("action:energy/collect", metrics);
        return Map.of("ok", true, "delta", Math.round(delta));
    }

    @PostMapping("/threats/simulate")
    public Map<String, Object> simulateThreat() {
        Map<String, Object> metrics = currentMetrics();
        long threats = asLong(metrics.get("threats"));
        metrics.put("threats", threats + 1);
        // leichte Temperaturänderung für Sichtbarkeit
        double temp = asDouble(metrics.get("temperature"));
        metrics.put("temperature", Math.max(0, Math.round((temp + (Math.random() > 0.5 ? 0.2 : -0.2)) * 10.0) / 10.0));
        pushTelemetry("action:threats/simulate", metrics);
        return Map.of("ok", true, "threats", threats + 1);
    }

    @PostMapping("/seed")
    public Map<String, Object> seed() {
        // Nur Bestätigung + kleiner Impuls für Telemetrie
        Map<String, Object> metrics = currentMetrics();
        double energy = asDouble(metrics.get("energy"));
        metrics.put("energy", Math.max(0, energy + 10));
        pushTelemetry("action:seed", metrics);
        return Map.of("ok", true);
    }

    // === helpers ===
    @SuppressWarnings("unchecked")
    private Map<String, Object> currentMetrics() {
        try {
            ResponseEntity<Map<String, Object>> res = telemetry.status();
            Map<String, Object> snap = res.getBody();
            if (snap == null) snap = new LinkedHashMap<>();
            Object m = snap.get("metrics");
            if (m instanceof Map<?, ?>) {
                return new LinkedHashMap<>((Map<String, Object>) m);
            }
        } catch (Exception ignored) {}
        // Fallback-Basiswerte
        Map<String, Object> base = new LinkedHashMap<>();
        base.put("energy", 3000);
        base.put("threats", 0);
        base.put("temperature", 23);
        return base;
    }

    private void pushTelemetry(String source, Map<String, Object> metrics) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("source", source);
        payload.put("metrics", metrics);
        try {
            telemetry.push(payload);
        } catch (Exception ignored) { /* best-effort */ }
    }

    private static double asDouble(Object o) {
        if (o instanceof Number) return ((Number) o).doubleValue();
        try { return Double.parseDouble(String.valueOf(o)); } catch (Exception e) { return 0.0; }
    }
    private static long asLong(Object o) {
        if (o instanceof Number) return ((Number) o).longValue();
        try { return Long.parseLong(String.valueOf(o)); } catch (Exception e) { return 0L; }
    }
}
