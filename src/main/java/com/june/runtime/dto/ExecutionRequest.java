package com.june.runtime.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ExecutionRequest {
    private String code;
    private Map<String, String> environmentVariables;
    private int timeoutSeconds = 30;
}
