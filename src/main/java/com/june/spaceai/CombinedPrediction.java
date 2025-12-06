package com.june.spaceai;

/**
 * Ergebnis der kombinierten Vorhersage.
 */
public class CombinedPrediction {
    public double optimizedValue;

    public CombinedPrediction(double optimizedValue) {
        this.optimizedValue = optimizedValue;
    }

    @Override
    public String toString() {
        return "Optimierter Wert: " + optimizedValue;
    }
}
