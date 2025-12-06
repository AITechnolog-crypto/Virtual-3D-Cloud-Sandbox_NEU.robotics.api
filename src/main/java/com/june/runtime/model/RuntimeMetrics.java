package com.june.runtime.model;

import lombok.Data;

@Data
public class RuntimeMetrics {
    long executions = 0;
    long successCount = 0;
    long totalDuration = 0;
    long lastExecution = 0;
}
