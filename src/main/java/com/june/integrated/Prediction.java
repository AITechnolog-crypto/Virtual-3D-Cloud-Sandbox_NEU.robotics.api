package com.june.integrated;

/**
 * Vorhersage-Ergebnis eines ML-Modells.
 */
public final class Prediction {
    private final double predictedValue;
    private final double confidence;

    public Prediction(double predictedValue, double confidence) {
        this.predictedValue = predictedValue;
        this.confidence = confidence;
    }

    public double getPredictedValue() { return predictedValue; }
    public double getConfidence() { return confidence; }

    @Override
    public String toString() {
        return "Vorhersage: " + predictedValue + ", Konfidenz: " + confidence;
    }
}
