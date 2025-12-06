package com.june.model.painrelief;

import com.june.service.painrelief.AdvancedBackPainReliefSystem;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TreatmentSession {
    private String sessionId;
    private String patientName;
    private String status;
    private AdvancedBackPainReliefSystem system;
    private List<String> logs;
    private long startTime;
    private boolean active;
    private boolean treatmentActive;
    private int activeDrones;
    private double currentPainLevel;
    private int treatmentsCompleted;
    private double painReduction;

    public TreatmentSession(String sessionId, String patientName, AdvancedBackPainReliefSystem system) {
        this.sessionId = sessionId;
        this.patientName = patientName;
        this.system = system;
        this.status = "INITIALISIERT";
        this.logs = new ArrayList<>();
        this.startTime = System.currentTimeMillis();
        this.active = false;
        this.treatmentActive = false;
        this.activeDrones = 0;
        this.currentPainLevel = 8.0;
        this.treatmentsCompleted = 0;
        this.painReduction = 0.0;
    }

    public void addLog(String message) {
        logs.add(message);
        if (logs.size() > 100) {
            logs.remove(0);
        }
    }

    public synchronized void incrementActiveDrones() {
        activeDrones++;
    }

    public synchronized void decrementActiveDrones() {
        activeDrones--;
    }

    public synchronized void incrementTreatmentsCompleted() {
        treatmentsCompleted++;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("sessionId", sessionId);
        map.put("patientName", patientName);
        map.put("status", status);
        map.put("active", active);
        map.put("treatmentActive", treatmentActive);
        map.put("activeDrones", activeDrones);
        map.put("currentPainLevel", currentPainLevel);
        map.put("treatmentsCompleted", treatmentsCompleted);
        map.put("painReduction", painReduction);
        map.put("logs", logs);
        map.put("uptime", System.currentTimeMillis() - startTime);
        return map;
    }
}
