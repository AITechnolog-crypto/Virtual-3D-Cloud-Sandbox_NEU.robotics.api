package com.june.controller.api;

import com.june.treasury.IntegratedTreasurySystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 💎 Treasury System REST Controller
 *
 * API für Schatzkammer-Management:
 *  - POST /api/treasury/activate - Aktiviert System
 *  - GET  /api/treasury/status   - Status aller Komponenten
 *
 * @author Sanel Crnkic - NurSystem Pro
 * @version 1.0
 */
@RestController
@RequestMapping("/api/treasury")
@CrossOrigin(origins = {"http://localhost:63342", "http://127.0.0.1:63342", "*"}, allowCredentials = "true")
public class TreasurySystemController {

    private final IntegratedTreasurySystem treasurySystem;

    @Autowired
    public TreasurySystemController(IntegratedTreasurySystem treasurySystem) {
        this.treasurySystem = treasurySystem;
        System.out.println("✅ TreasurySystemController initialisiert");
    }

    /**
     * POST /api/treasury/activate
     * Aktiviert das Treasury System für einen Standort
     */
    @PostMapping(value = "/activate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> activate(@RequestBody(required = false) Map<String, String> request) {
        Map<String, Object> response = new LinkedHashMap<>();

        try {
            String location = request != null ?
                request.getOrDefault("location", "Central World Bank Location") :
                "Central World Bank Location";

            treasurySystem.activateSystem(location);

            response.put("success", true);
            response.put("message", "Treasury System aktiviert");
            response.put("location", location);
            response.put("timestamp", System.currentTimeMillis());

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/treasury/status
     * Gibt Status aller Treasury-Komponenten zurück
     */
    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("success", true);
        response.put("status", treasurySystem.getSystemStatus());
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }
}
