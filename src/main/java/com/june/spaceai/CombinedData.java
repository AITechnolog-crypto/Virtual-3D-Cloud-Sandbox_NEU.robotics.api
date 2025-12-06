package com.june.spaceai;

/**
 * Kombination aus Energie- und Satellitendaten.
 */
public class CombinedData {
    public double energyLevel;
    public double signalStrength;

    public CombinedData(double energyLevel, double signalStrength) {
        this.energyLevel = energyLevel;
        this.signalStrength = signalStrength;
    }

    @Override
    public String toString() {
        return "Kombinierte Daten: Energielevel=" + energyLevel + ", Signalstärke=" + signalStrength;
    }
}
