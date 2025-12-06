package com.june.model.painrelief.tens;

import com.june.service.painrelief.tens.AdvancedBackPainReliefSystem;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TherapySession {
    private String sessionId;
    private String patientName;
    private String status;
    private AdvancedBackPainReliefSystem system;
    private List<String> logs;
    private long startTime;
    private boolean active;
    private int activeDrones;
    private double currentPainLevel;
    private int treatmentsCompleted;
    private boolean tensActive;
    private double tensFrequency;
    private double tensIntensity;
    private boolean emsActive;
    private double emsIntensity;
    private double gpsAccuracy;

    public TherapySession(String sessionId, String patientName, AdvancedBackPainReliefSystem system) {
        this.sessionId = sessionId;
        this.patientName = patientName;
        this.system = system;
        this.status = "INITIALISIERT";
        this.logs = new ArrayList<>();
        this.startTime = System.currentTimeMillis();
        this.active = false;
        this.activeDrones = 0;
        this.currentPainLevel = 8.0;
        this.treatmentsCompleted = 0;
        this.tensActive = false;
        this.tensFrequency = 0.0;
        this.tensIntensity = 0.0;
        this.emsActive = false;
        this.emsIntensity = 0.0;
        this.gpsAccuracy = 0.0;
    }

    public void addLog(String message) {
        logs.add(message);
        if (logs.size() > 100) {
            logs.remove(0);
        }
    }

    public synchronized void incrementActiveDrones() { activeDrones++; }
    public synchronized void incrementTreatmentsCompleted() { treatmentsCompleted++; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("sessionId", sessionId);
        map.put("patientName", patientName);
        map.put("status", status);
        map.put("active", active);
        map.put("activeDrones", activeDrones);
        map.put("currentPainLevel", currentPainLevel);
        map.put("treatmentsCompleted", treatmentsCompleted);
        map.put("tensActive", tensActive);
        map.put("tensFrequency", tensFrequency);
        map.put("tensIntensity", tensIntensity);
        map.put("emsActive", emsActive);
        map.put("emsIntensity", emsIntensity);
        map.put("gpsAccuracy", gpsAccuracy);
        map.put("logs", logs);
        map.put("uptime", System.currentTimeMillis() - startTime);
        return map;
    }
}
