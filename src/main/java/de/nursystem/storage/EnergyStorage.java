package de.nursystem.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Einzelner Energie-Speicher
 */
public class EnergyStorage implements IEnergyStorage {
    private static final Logger logger = LoggerFactory.getLogger(EnergyStorage.class);

    private final String location;
    private final double maxCapacity; // in kWh
    private double storedEnergy; // in kWh
    private List<Energy> energyHistory;

    public EnergyStorage(String location) {
        this(location, 100000); // Default: 100.000 kWh Kapazität
    }

    public EnergyStorage(String location, double maxCapacity) {
        this.location = location;
        this.maxCapacity = maxCapacity;
        this.storedEnergy = 0;
        this.energyHistory = new ArrayList<>();
    }

    @Override
    public void storeEnergy(Energy energy) {
        double remainingCapacity = maxCapacity - storedEnergy;

        if (remainingCapacity > 0) {
            double toStore = Math.min(energy.getAmount(), remainingCapacity);
            storedEnergy += toStore;
            energyHistory.add(energy);

            logger.debug("  💾 {} speichert {} kWh ({}/{})",
                location, String.format("%.2f", toStore),
                String.format("%.0f", storedEnergy),
                String.format("%.0f", maxCapacity));
        } else {
            logger.warn("  ⚠️ {} ist voll! Kapazität erreicht.", location);
        }
    }

    @Override
    public Energy retrieveEnergy(double amount) {
        if (storedEnergy >= amount) {
            storedEnergy -= amount;
            logger.info("  📤 {} gibt {} kWh ab", location, String.format("%.2f", amount));
            return new Energy(EnergyType.SOLAR, amount, location);
        } else {
            logger.warn("  ⚠️ {} hat nicht genug Energie", location);
            return null;
        }
    }

    @Override
    public double getTotalStoredEnergy() {
        return storedEnergy;
    }

    @Override
    public Map<String, Double> getStorageByLocation() {
        Map<String, Double> map = new HashMap<>();
        map.put(location, storedEnergy);
        return map;
    }

    public String getLocation() { return location; }
    public double getCapacityPercentage() { return (storedEnergy / maxCapacity) * 100; }
}
