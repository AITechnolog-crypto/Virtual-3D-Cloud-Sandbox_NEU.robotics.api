package com.june.simulation.energy;

/**
 * Energiespeicher-Implementierung für die Antarktis mit Kapazitätsprüfung.
 */
public class AntarcticEnergyStorage extends AbstractEnergyStorage {
    public AntarcticEnergyStorage(double maxCapacity) {
        super(maxCapacity);
    }

    @Override
    public boolean storeEnergy(Energy energy) {
        if (energy == null || !Double.isFinite(energy.amount) || energy.amount <= 0) {
            return false;
        }
        if (currentCapacity + energy.amount <= maxCapacity) {
            currentCapacity += energy.amount;
            logger.info("Energie in der Antarktis gespeichert: " + energy +
                    ". Aktuelle Kapazität: " + currentCapacity + "/" + maxCapacity);
            return true;
        } else {
            logger.warning("Nicht genügend Speicherkapazität in der Antarktis für: " + energy +
                    ". Aktuell: " + currentCapacity + ", Max: " + maxCapacity);
            return false;
        }
    }
}
