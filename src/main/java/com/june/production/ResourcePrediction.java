package com.june.production;

/**
 * Einfache Vorhersageklasse für Ressourcenverteilung/Produktion.
 */
public class ResourcePrediction {
    private double expectedThroughput; // z. B. Einheiten pro Zyklus
    private double confidence;         // 0..1

    public ResourcePrediction() {
    }

    public ResourcePrediction(double expectedThroughput, double confidence) {
        this.expectedThroughput = expectedThroughput;
        this.confidence = confidence;
    }

    public double getExpectedThroughput() {
        return expectedThroughput;
    }

    public void setExpectedThroughput(double expectedThroughput) {
        this.expectedThroughput = expectedThroughput;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    @Override
    public String toString() {
        return "ResourcePrediction{" +
                "expectedThroughput=" + expectedThroughput +
                ", confidence=" + confidence +
                '}';
    }
}