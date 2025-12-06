package de.nursystem.reconstruction;

import java.util.HashMap;
import java.util.Map;

/**
 * Qualitätsbericht
 */
class QualityReport {
    private final String siteName;
    private final Map<String, Double> checks = new HashMap<>();

    public QualityReport(String siteName) {
        this.siteName = siteName;
    }

    public void addCheck(String category, double score) {
        checks.put(category, score);
    }

    public double getAverageQuality() {
        return checks.values().stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
    }
}
