package com.june.energy;

/**
 * Einfache Rechenzentrums-Implementierung.
 */
public class ComputeCenter implements IComputeCenter {
    private final String location;

    public ComputeCenter(String location) {
        this.location = location == null ? "unknown" : location;
    }

    @Override
    public void processTasks() {
        // Platzhalter für echte Auftragsverarbeitung, Queue/Batch/Stream etc.
        System.out.println("Processing tasks at " + location);
    }

    public String getLocation() {
        return location;
    }
}
