package com.june.ai.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PredictRequest {
    private String owner;
    private List<Map<String, Object>> historicalData;
}
