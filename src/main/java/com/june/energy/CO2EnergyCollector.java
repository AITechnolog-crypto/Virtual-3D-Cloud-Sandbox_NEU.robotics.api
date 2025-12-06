package com.june.energy;

/**
 * Beispielhafter Kollektor für Energiegewinnung durch CO2-Umwandlung.
 */
public class CO2EnergyCollector implements IEnergyCollector {
    @Override
    public Energy collectEnergy() {
        // Platzhalter-Logik. In der Realität: chemische Prozesse / Katalysatoren / Energie-Bilanz
        return new Energy(EnergyType.CO2, 500);
    }
}
