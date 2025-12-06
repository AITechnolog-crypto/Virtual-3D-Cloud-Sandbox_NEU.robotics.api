package com.june.factory;

/**
 * Energiespeicher-POJO für die Produktionsfabrik.
 */
public class EnergyStorage {
    private final String type;
    private final int capacity;

    public EnergyStorage(String type, int capacity) {
        this.type = type;
        this.capacity = capacity;
    }

    @Override
    public String toString() {
        return "Energiespeicher: Typ=" + type + ", Kapazität=" + capacity;
    }
}
