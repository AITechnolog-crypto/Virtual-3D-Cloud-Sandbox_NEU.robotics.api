package com.june.controller.api;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * LanguagePolicyController
 * - Policy: German-only language setting persisted in-memory with default from app.language
 * - GET  /api/policy/lang  → { language:"de", policy:"GERMAN_ONLY" }
 * - POST /api/policy/lang  → accepts { language:"de" }, enforces German-only policy
 */
@RestController
@RequestMapping("/api/policy")
@CrossOrigin(origins = "*")
public class LanguagePolicyController {

    private volatile String language;
    private final String policy = "GERMAN_ONLY";

    public LanguagePolicyController(Environment env) {
        String configured = env != null ? env.getProperty("app.language", "de") : "de";
        this.language = (configured == null || configured.isBlank()) ? "de" : configured.toLowerCase(Locale.ROOT);
        if (!"de".equals(this.language)) {
            // Enforce German-only on startup as per policy
            this.language = "de";
        }
    }

    @GetMapping(value = "/lang", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getLanguage() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("language", language);
        m.put("policy", policy);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(m);
    }

    @PostMapping(value = "/lang", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> setLanguage(@RequestBody Map<String, Object> payload) {
        String requested = Objects.toString(payload.get("language"), "").trim().toLowerCase(Locale.ROOT);
        Map<String, Object> m = new LinkedHashMap<>();
        if (!"de".equals(requested)) {
            m.put("error", "Nur Deutsch (de) gemäß Policy erlaubt");
            m.put("policy", policy);
            m.put("language", language);
            return ResponseEntity.badRequest().body(m);
        }
        this.language = "de";
        m.put("ok", true);
        m.put("language", language);
        m.put("policy", policy);
        return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store").body(m);
    }
}
