package com.june.energy;

/**
 * Beispielhafter Kollektor für Solarenergie.
 */
public class SolarEnergyCollector implements IEnergyCollector {
    @Override
    public Energy collectEnergy() {
        // Platzhalter-Logik. In der Realität: Sensordaten / Vorhersage / Steuerungslogik
        return new Energy(EnergyType.SOLAR, 1000);
    }
}
