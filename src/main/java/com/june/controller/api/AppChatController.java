package com.june.controller.api;

import com.june.service.chat.SessionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AppChatController {

    private final RestTemplate restTemplate;
    private final SessionService sessions;

    @Value("${azure.openai.endpoint:}")
    private String azureEndpoint;

    @Value("${azure.openai.key:}")
    private String azureKey;

    @Value("${azure.openai.deployment:}")
    private String deployment;

    public AppChatController(RestTemplate restTemplate, SessionService sessions) {
        this.restTemplate = restTemplate;
        this.sessions = sessions;
    }

    @PostMapping("/consent")
    public ResponseEntity<Map<String, String>> consent(@RequestBody Map<String, Object> body) {
        String sessionId = String.valueOf(body.getOrDefault("sessionId", "anonymous"));
        sessions.setConsent(sessionId, true);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/warmup")
    public ResponseEntity<Map<String, String>> warmup(@RequestBody Map<String, Object> body) {
        String sessionId = String.valueOf(body.getOrDefault("sessionId", "anonymous"));
        sessions.touch(sessionId);
        return ResponseEntity.ok(Map.of("status", "warmup requested"));
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, Object> body) {
        String sessionId = String.valueOf(body.getOrDefault("sessionId", "anonymous"));
        String userMessage = String.valueOf(body.getOrDefault("message", "")).trim();
        String roleHint = String.valueOf(body.getOrDefault("roleHint", "assistant"));

        if (userMessage.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("reply", "Leere Nachricht"));
        }
        if (!sessions.hasConsent(sessionId)) {
            return ResponseEntity.status(403).body(Map.of("reply", "Konsent erforderlich. Bitte akzeptiere die Bedingungen im Chat."));
        }
        if (sessions.isRateLimited(sessionId)) {
            return ResponseEntity.status(429).body(Map.of("reply", "Zu viele Anfragen. Bitte warte kurz."));
        }
        if (looksDangerous(userMessage)) {
            return ResponseEntity.ok(Map.of("reply", "Entschuldigung, dabei kann ich nicht helfen. Bitte formuliere die Anfrage anders."));
        }

        // Build messages with a persistent system-prompt
        List<Map<String, String>> history = sessions.getMessages(sessionId);
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(
                "role", "system",
                "content", "Du bist ein ruhiger, hilfreicher Shopmanager-Assistent. Antworte knapp, gib Produktinformationen, Bestellhilfe, Preise und einfache administrative Anweisungen. Frage nach Klarstellung wenn nötig. Nie automatisch Aktionen ohne menschliche Bestätigung.")
        );
        int keep = Math.max(0, history.size() - 10);
        for (int i = keep; i < history.size(); i++) messages.add(history.get(i));
        messages.add(Map.of("role", "user", "content", userMessage));
        sessions.addMessage(sessionId, "user", userMessage);

        // If not configured: local fallback
        if (azureEndpoint == null || azureEndpoint.isBlank() || azureKey == null || azureKey.isBlank() || deployment == null || deployment.isBlank()) {
            String fallback = localReply(roleHint, userMessage);
            sessions.addMessage(sessionId, "assistant", fallback);
            return ResponseEntity.ok(Map.of("reply", fallback));
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", deployment);
        payload.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", azureKey);

        HttpEntity<Map<String, Object>> req = new HttpEntity<>(payload, headers);
        try {
            String url = azureEndpoint + "/openai/deployments/" + deployment + "/chat/completions?api-version=2023-10-01-preview";
            ResponseEntity<Map> resp = restTemplate.postForEntity(url, req, Map.class);
            String reply = extractReply(resp.getBody());
            if (reply == null || reply.isEmpty()) reply = "Keine Antwort erhalten";
            sessions.addMessage(sessionId, "assistant", reply);
            return ResponseEntity.ok(Map.of("reply", reply));
        } catch (Exception e) {
            String err = "Fehler beim Kontakt mit Modell: " + e.getMessage();
            sessions.addMessage(sessionId, "assistant", err);
            return ResponseEntity.status(500).body(Map.of("reply", err));
        }
    }

    private boolean looksDangerous(String text) {
        String low = text.toLowerCase(Locale.ROOT);
        return low.contains("bomb") || low.contains("waffe") || low.contains("anleitung zum") || low.contains("jackpot hack");
    }

    @SuppressWarnings("rawtypes")
    private String extractReply(Map body) {
        if (body == null) return null;
        Object choices = body.get("choices");
        if (choices instanceof List && !((List) choices).isEmpty()) {
            Object first = ((List) choices).get(0);
            if (first instanceof Map) {
                Object msg = ((Map) first).get("message");
                if (msg instanceof Map) {
                    Object content = ((Map) msg).get("content");
                    return content == null ? null : String.valueOf(content);
                }
            }
        }
        return null;
    }

    private String localReply(String role, String msg) {
        String lower = (msg == null ? "" : msg.toLowerCase(Locale.ROOT));
        // Aiki-Regel laut Issue
        if (lower.contains("plan")) {
            return "🌸 Aiki flüstert: 'Ein Plan ist wie Tau am Morgen – handle sanft, und er bleibt bestehen.'";
        }
        if (lower.contains("öffnungszeiten")) return "Der Shop ist täglich von 9 bis 19 Uhr geöffnet.";
        if (lower.contains("preis") || lower.contains("kosten")) return "Bitte nenne mir das Produkt oder die Produktnummer, dann sage ich dir den Preis.";
        if (lower.contains("bestellung") || lower.contains("status")) return "Ich kann Bestellstatus prüfen. Nenne mir bitte die Bestellnummer.";
        return "🌙 Aiki: 'Deine Eingabe wurde in die Wolken geschrieben. Alles ist bereit.'";
    }
}
