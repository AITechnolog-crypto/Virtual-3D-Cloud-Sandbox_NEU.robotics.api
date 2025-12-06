package com.june.spaceai;

/**
 * Datenklasse: Repräsentiert Energiedaten aus dem Weltraum.
 */
public class ResourceData {
    public double energyLevel;

    public ResourceData(double energyLevel) {
        this.energyLevel = energyLevel;
    }

    @Override
    public String toString() {
        return "Energielevel: " + energyLevel;
    }
}
