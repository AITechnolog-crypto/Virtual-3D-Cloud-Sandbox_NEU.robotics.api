package com.june.simulation.energy;

import java.util.logging.Logger;

/**
 * Abstrakte Basis für Energiespeicher mit Kapazitätsverwaltung und Logging.
 */
public abstract class AbstractEnergyStorage implements IEnergyStorageV2 {
    protected double currentCapacity = 0.0;
    protected final double maxCapacity;
    protected final Logger logger = Logger.getLogger(getClass().getName());

    public AbstractEnergyStorage(double maxCapacity) {
        if (maxCapacity <= 0 || !Double.isFinite(maxCapacity)) {
            throw new IllegalArgumentException("maxCapacity muss > 0 und endlich sein");
        }
        this.maxCapacity = maxCapacity;
    }

    @Override
    public double getCurrentCapacity() {
        return currentCapacity;
    }

    @Override
    public double getMaxCapacity() {
        return maxCapacity;
    }
}
