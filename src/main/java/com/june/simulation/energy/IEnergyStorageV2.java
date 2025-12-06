package com.june.simulation.energy;

/**
 * Interface für Energiespeicher (V2), angepasst an die Anforderung.
 */
public interface IEnergyStorageV2 {
    /**
     * Speichert Energie und gibt true zurück, wenn erfolgreich (Kapazität vorhanden).
     */
    boolean storeEnergy(Energy energy);

    double getCurrentCapacity();
    double getMaxCapacity();
}
