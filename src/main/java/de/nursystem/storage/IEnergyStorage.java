package de.nursystem.storage;

import java.util.Map;

/**
 * Energie-Speicher Interface
 */
public interface IEnergyStorage {
    void storeEnergy(Energy energy);
    Energy retrieveEnergy(double amount);
    double getTotalStoredEnergy();
    Map<String, Double> getStorageByLocation();
}
