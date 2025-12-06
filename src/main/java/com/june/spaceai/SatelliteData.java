package com.june.spaceai;

/**
 * Datenklasse: Repräsentiert Satellitensignaldaten.
 */
public class SatelliteData {
    public double signalStrength;

    public SatelliteData(double signalStrength) {
        this.signalStrength = signalStrength;
    }

    @Override
    public String toString() {
        return "Signalstärke: " + signalStrength;
    }
}
