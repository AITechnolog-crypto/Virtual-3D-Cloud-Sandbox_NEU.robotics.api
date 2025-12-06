package com.june.ai.model;

import java.util.HashMap;
import java.util.Map;

public class PredictionResult {
    public final double prediction;
    public final double confidence;
    public final String message;

    public PredictionResult(double prediction, double confidence, String message) {
        this.prediction = prediction;
        this.confidence = confidence;
        this.message = message;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("prediction", prediction);
        map.put("confidence", confidence);
        map.put("message", message);
        return map;
    }
}
