package com.june.robotics.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Minimaler API-Controller nahe bei den Robotics-Klassen.
 *
 * Hinweis:
 * - Keine "Lebensgefahr"-Endpunkte (Emergency) – explizit ausgelassen.
 * - "Roboter macht es selbst": Wir simulieren interne Abarbeitung als Stub.
 * - Keine Änderungen an bestehenden Dateien. Rein additive, eigenständige Klasse.
 */
@RestController
@RequestMapping("/api/robot")
public class GuardianRobotController {

    private final Map<String, RobotTask> tasks = new ConcurrentHashMap<>();

    /* -------------------- Health -------------------- */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "OK",
                "service", "guardian-robot-api",
                "time", Instant.now().toString()
        );
    }

    /* -------------------- Bestellung -------------------- */
    @PostMapping("/order")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody OrderRequest req) {
        if (req == null || !StringUtils.hasText(req.getSku())) {
            return badRequest("sku fehlt");
        }
        String id = newId("ord");
        RobotTask t = RobotTask.of(id, TaskType.ORDER, "IN_PROGRESS",
                String.format(Locale.US, "Bestellung %s wird vom Roboter vorbereitet.", req.getSku()));
        tasks.put(id, t);
        simulateProgress(t);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", id,
                "status", t.status,
                "message", t.message
        ));
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<Map<String, Object>> getStatus(@PathVariable String id) {
        RobotTask t = tasks.get(id);
        if (t == null) return notFound("Task nicht gefunden");
        return ResponseEntity.ok(taskPayload(t));
    }

    /* -------------------- Testlauf -------------------- */
    @PostMapping("/test-run")
    public ResponseEntity<Map<String, Object>> createTestRun(@RequestBody(required = false) TestRunRequest req) {
        String scenario = req != null && StringUtils.hasText(req.getScenario()) ? req.getScenario() : "default";
        String id = newId("tes");
        RobotTask t = RobotTask.of(id, TaskType.TEST, "IN_PROGRESS",
                String.format(Locale.US, "Testlauf '%s' gestartet. Roboter führt eigenständig aus.", scenario));
        tasks.put(id, t);
        simulateProgress(t);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", id,
                "status", t.status,
                "message", t.message
        ));
    }

    @GetMapping("/test-run/{id}")
    public ResponseEntity<Map<String, Object>> getTestRun(@PathVariable String id) {
        RobotTask t = tasks.get(id);
        if (t == null) return notFound("Testlauf nicht gefunden");
        return ResponseEntity.ok(taskPayload(t));
    }

    /* -------------------- Forschung -------------------- */
    @PostMapping("/research")
    public ResponseEntity<Map<String, Object>> createResearch(@RequestBody ResearchRequest req) {
        if (req == null || !StringUtils.hasText(req.getTopic())) {
            return badRequest("topic fehlt");
        }
        String id = newId("res");
        RobotTask t = RobotTask.of(id, TaskType.RESEARCH, "IN_PROGRESS",
                String.format(Locale.US, "Forschungsauftrag '%s' übernommen. Roboter sammelt Daten.", req.getTopic()));
        tasks.put(id, t);
        simulateProgress(t);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", id,
                "status", t.status,
                "message", t.message
        ));
    }

    @GetMapping("/research/{id}")
    public ResponseEntity<Map<String, Object>> getResearch(@PathVariable String id) {
        RobotTask t = tasks.get(id);
        if (t == null) return notFound("Forschungsauftrag nicht gefunden");
        return ResponseEntity.ok(taskPayload(t));
    }

    /* -------------------- Hilfsfunktionen -------------------- */
    private Map<String, Object> taskPayload(RobotTask t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.id);
        m.put("type", t.type.name());
        m.put("status", t.status);
        m.put("message", t.message);
        m.put("createdAt", t.createdAt.toString());
        if (t.completedAt != null) m.put("completedAt", t.completedAt.toString());
        return m;
    }

    private ResponseEntity<Map<String, Object>> notFound(String msg) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", msg));
    }

    private ResponseEntity<Map<String, Object>> badRequest(String msg) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", msg));
    }

    private String newId(String prefix) {
        return prefix + "-" + Long.toHexString(ThreadLocalRandom.current().nextLong()).replace('-', 'x');
    }

    private void simulateProgress(RobotTask t) {
        // Sehr einfache, nicht-blockierende Fortschritts-Simulation über Timer
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override public void run() {
                if (!"IN_PROGRESS".equals(t.status)) { cancel(); return; }
                t.status = "DONE";
                t.completedAt = Instant.now();
                t.message = switch (t.type) {
                    case ORDER -> "Bestellung abgeschlossen. Paket vorbereitet.";
                    case TEST -> "Testlauf erfolgreich beendet.";
                    case RESEARCH -> "Forschungsdaten gesammelt und gespeichert.";
                };
                cancel();
            }
        }, 1200L); // ~1,2 Sek. bis Abschluss
    }

    /* -------------------- DTOs & Model (intern) -------------------- */
    enum TaskType { ORDER, TEST, RESEARCH }

    static final class RobotTask {
        final String id;
        final TaskType type;
        volatile String status;
        volatile String message;
        final Instant createdAt;
        volatile Instant completedAt;
        private RobotTask(String id, TaskType type, String status, String message) {
            this.id = id; this.type = type; this.status = status; this.message = message; this.createdAt = Instant.now();
        }
        static RobotTask of(String id, TaskType type, String status, String message) {
            return new RobotTask(id, type, status, message);
        }
    }

    public static final class OrderRequest {
        private String sku;
        private String buyerEmail; // optional
        public String getSku() { return sku; }
        public void setSku(String sku) { this.sku = sku; }
        public String getBuyerEmail() { return buyerEmail; }
        public void setBuyerEmail(String buyerEmail) { this.buyerEmail = buyerEmail; }
    }

    public static final class TestRunRequest {
        private String scenario; // optional
        public String getScenario() { return scenario; }
        public void setScenario(String scenario) { this.scenario = scenario; }
    }

    public static final class ResearchRequest {
        private String topic;
        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
    }
}
