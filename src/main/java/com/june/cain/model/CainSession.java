package com.june.cain.model;

import com.june.cain.Cain;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CainSession {
    private String sessionId;
    private String region;
    private String satelliteProvider;
    private String status;
    private Cain cainSystem;
    private List<String> logs;
    private boolean active;

    // Statistiken
    private int dataDownloads;
    private int environmentalUpdates;
    private int sustainabilityAnalyses;
    private int errors;
    private int warnings;
    private double dataQuality;
    private double sustainabilityScore;

    public CainSession(String sessionId, String region, String satelliteProvider, Cain cainSystem) {
        this.sessionId = sessionId;
        this.region = region;
        this.satelliteProvider = satelliteProvider;
        this.cainSystem = cainSystem;
        this.status = "INITIALISIERT";
        this.logs = new ArrayList<>();
        this.active = false;
        this.dataDownloads = 0;
        this.environmentalUpdates = 0;
        this.sustainabilityAnalyses = 0;
        this.errors = 0;
        this.warnings = 0;
        this.dataQuality = 0.0;
        this.sustainabilityScore = 0.0;
    }

    public void addLog(String message) {
        logs.add(message);
        if (logs.size() > 200) logs.remove(0);
    }

    public synchronized void incrementDataDownloads() { dataDownloads++; }
    public synchronized void incrementEnvironmentalUpdates() { environmentalUpdates++; }
    public synchronized void incrementSustainabilityAnalyses() { sustainabilityAnalyses++; }
    public synchronized void incrementErrors() { errors++; }
    public synchronized void incrementWarnings() { warnings++; } // Syntaxfehler behoben

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("sessionId", sessionId);
        map.put("region", region);
        map.put("satelliteProvider", satelliteProvider);
        map.put("status", status);
        map.put("active", active);
        map.put("dataDownloads", dataDownloads);
        map.put("environmentalUpdates", environmentalUpdates);
        map.put("sustainabilityAnalyses", sustainabilityAnalyses);
        map.put("errors", errors);
        map.put("warnings", warnings);
        map.put("dataQuality", dataQuality);
        map.put("sustainabilityScore", sustainabilityScore);
        map.put("logs", logs);
        return map;
    }
}
