package com.june.cloud.components;

import org.springframework.stereotype.Component;

/**
 * Platzhalter für AnalyticsEngine
 */
@Component
class AnalyticsEngine {
    public void analyze(Dataset dataset) { /* Simuliert */ }
    public String getStatus() { return "AnalyticsEngine: Running"; }
}
