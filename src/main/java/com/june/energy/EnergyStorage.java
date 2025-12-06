package com.june.collection2;

import java.util.logging.Logger;

public class EnergyStorage implements IEnergyStorage {
    private final Logger logger = Logger.getLogger(EnergyStorage.class.getName());
    private double storedEnergy = 0;

    @Override
    public void storeEnergy(Energy energy) {
        if (energy == null || !Double.isFinite(energy.amount)) return;
        storedEnergy += energy.amount;
        logger.info("Energie gespeichert. Aktueller Stand: " + storedEnergy + " " + energy.unit);
    }
}
