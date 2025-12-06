package com.june.ai.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PredictionJob {
    private String id;
    private String status;
    private long startTime;
    private long endTime;
    private List<Map<String, Object>> inputs;
    private List<Map<String, Object>> results;
    private String modelName;

    public PredictionJob(String id, String modelName, List<String> inputs) {
        this.id = id;
        this.modelName = modelName;
        this.status = "CREATED";
        this.startTime = System.currentTimeMillis();
        this.inputs = new ArrayList<>();
        for (String input : inputs) {
            this.inputs.add(Map.of("text", input));
        }
        this.results = new ArrayList<>();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("status", status);
        map.put("modelName", modelName);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        map.put("duration", endTime > 0 ? endTime - startTime : 0);
        map.put("inputCount", inputs.size());
        map.put("resultCount", results.size());
        map.put("results", results);
        return map;
    }

    // Getters & Setters
    public String getId() { return id; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public void setEndTime(long endTime) { this.endTime = endTime; }
    public List<Map<String, Object>> getInputs() { return inputs; }
    public void addResult(Map<String, Object> result) { results.add(result); }
    public List<Map<String, Object>> getResults() { return results; }
}
