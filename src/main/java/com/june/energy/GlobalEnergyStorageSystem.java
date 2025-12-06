package com.june.energy;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Einfache globale Energiespeicherung mit Aggregation nach Typ.
 * Thread-sicher mittels Synchronisierung auf der internen Map.
 */
public class GlobalEnergyStorageSystem implements IEnergyStorage {
    private final EnumMap<EnergyType, Double> store = new EnumMap<>(EnergyType.class);

    @Override
    public void storeEnergy(Energy energy) {
        if (energy == null) return;
        synchronized (store) {
            double prev = store.getOrDefault(energy.type(), 0.0);
            store.put(energy.type(), prev + energy.amount());
        }
    }

    /**
     * Liefert die aktuell gespeicherte Energiemenge eines Typs.
     */
    public double getAmount(EnergyType type) {
        synchronized (store) {
            return store.getOrDefault(type, 0.0);
        }
    }

    /**
     * Gesamte Energiemenge über alle Typen.
     */
    public double getTotal() {
        synchronized (store) {
            return store.values().stream().mapToDouble(Double::doubleValue).sum();
        }
    }

    /**
     * Liefert eine unveränderliche Kopie der aktuellen Speicherstände.
     */
    public Map<EnergyType, Double> snapshot() {
        synchronized (store) {
            return Collections.unmodifiableMap(new EnumMap<>(store));
        }
    }
}
