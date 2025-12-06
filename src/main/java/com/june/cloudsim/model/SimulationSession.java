package com.june.cloudsim.model;

import java.util.*;

public class SimulationSession {
    private String id;
    private String status;
    private long startTime;
    private long endTime;
    private Map<String, Object> results;
    private List<String> logs;

    public SimulationSession(String id) {
        this.id = id;
        this.status = "CREATED";
        this.startTime = System.currentTimeMillis();
        this.results = new HashMap<>();
        this.logs = new ArrayList<>();
    }

    public void addLog(String message) {
        logs.add("[" + new Date() + "] " + message);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("status", status);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        map.put("duration", endTime > 0 ? endTime - startTime : 0);
        map.put("results", results);
        map.put("logs", logs);
        return map;
    }

    // Getters & Setters
    public String getId() { return id; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public void setEndTime(long endTime) { this.endTime = endTime; }

    public Map<String, Object> getResults() { return results; }

    public void setResults(Map<String, Object> results) { this.results = results; }
}
