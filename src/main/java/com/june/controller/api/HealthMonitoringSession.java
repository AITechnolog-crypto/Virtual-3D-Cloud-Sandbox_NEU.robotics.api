package com.june.controller.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SESSION MANAGEMENT
 */
public class HealthMonitoringSession {
    private String sessionId;
    private String patientName;
    private String patientId;
    private String status;
    private HealthMonitoringAI healthAI;
    private List<String> logs;
    private long startTime;
    private boolean active;

    // Vitaldaten
    private int heartRate;
    private String bloodPressure;
    private double temperature;
    private int oxygenSaturation;
    private int respiratoryRate;
    private double healthScore;

    // Statistiken
    private int predictionCount;
    private int alerts;
    private int warnings;

    public HealthMonitoringSession(String sessionId, String patientName, String patientId, HealthMonitoringAI healthAI) {
        this.sessionId = sessionId;
        this.patientName = patientName;
        this.patientId = patientId;
        this.healthAI = healthAI;
        this.status = "INITIALISIERT";
        this.logs = new ArrayList<>();
        this.startTime = System.currentTimeMillis();
        this.active = false;
        this.heartRate = 0;
        this.bloodPressure = "0/0";
        this.temperature = 0.0;
        this.oxygenSaturation = 0;
        this.respiratoryRate = 0;
        this.healthScore = 100.0;
        this.predictionCount = 0;
        this.alerts = 0;
        this.warnings = 0;
    }

    public void addLog(String message) {
        logs.add(message);
        if (logs.size() > 200) logs.remove(0);
    }

    public synchronized void incrementPredictionCount() { predictionCount++; }
    public synchronized void incrementAlerts() { alerts++; }
    public synchronized void incrementWarnings() { warnings++; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("sessionId", sessionId);
        map.put("patientName", patientName);
        map.put("patientId", patientId);
        map.put("status", status);
        map.put("active", active);
        map.put("heartRate", heartRate);
        map.put("bloodPressure", bloodPressure);
        map.put("temperature", temperature);
        map.put("oxygenSaturation", oxygenSaturation);
        map.put("respiratoryRate", respiratoryRate);
        map.put("healthScore", healthScore);
        map.put("predictionCount", predictionCount);
        map.put("alerts", alerts);
        map.put("warnings", warnings);
        map.put("logs", logs);
        map.put("uptime", System.currentTimeMillis() - startTime);
        return map;
    }

    // Getters & Setters
    public String getSessionId() { return sessionId; }
    public String getPatientName() { return patientName; }
    public String getPatientId() { return patientId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public HealthMonitoringAI getHealthAI() { return healthAI; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getHeartRate() { return heartRate; }
    public void setHeartRate(int heartRate) { this.heartRate = heartRate; }
    public String getBloodPressure() { return bloodPressure; }
    public void setBloodPressure(String bloodPressure) { this.bloodPressure = bloodPressure; }
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public int getOxygenSaturation() { return oxygenSaturation; }
    public void setOxygenSaturation(int oxygenSaturation) { this.oxygenSaturation = oxygenSaturation; }
    public int getRespiratoryRate() { return respiratoryRate; }
    public void setRespiratoryRate(int respiratoryRate) { this.respiratoryRate = respiratoryRate; }
    public double getHealthScore() { return healthScore; }
    public void setHealthScore(double healthScore) { this.healthScore = healthScore; }
    public int getPredictionCount() { return predictionCount; }
    public int getAlerts() { return alerts; }
    public int getWarnings() { return warnings; }
}
