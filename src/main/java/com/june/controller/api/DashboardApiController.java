package com.june.controller.api.v1.simulation;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardApiController {

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();

        status.put("timestamp", System.currentTimeMillis());
        status.put("status", "online");

        // Counts (Placeholder – falls keine DB aktiv ist)
        status.put("robotCount", 0);
        status.put("droneCount", 0);
        status.put("walletCount", 0);
        status.put("transactionCount", 0);

        // System metrics
        Map<String, Object> system = new HashMap<>();
        system.put("swarmSize", "10.0B");
        system.put("energy", 2847);
        system.put("threats", 0);
        system.put("temperature", 23);
        status.put("system", system);

        return status;
    }

    // Deutscher Alias mit deutschsprachigen Schlüsseln
    @GetMapping("/status-de")
    public Map<String, Object> getStatusDeutsch() {
        Map<String, Object> status = new HashMap<>();

        status.put("zeitstempel", System.currentTimeMillis());
        status.put("status", "online");

        // Zähler (Platzhalter)
        status.put("roboterAnzahl", 0);
        status.put("drohnenAnzahl", 0);
        status.put("walletAnzahl", 0);
        status.put("transaktionsAnzahl", 0);

        // Systemmetriken
        Map<String, Object> system = new HashMap<>();
        system.put("schwarmGroesse", "10.0B");
        system.put("energie", 2847);
        system.put("bedrohungen", 0);
        system.put("temperatur", 23);
        status.put("system", system);

        return status;
    }
}
