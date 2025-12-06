package com.june.energy.solar;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * ☀️ Solarpanel mit Effizienz- und Kapazitätsmanagement
 * Absorbiert Sonnenlicht und erzeugt Energie
 */
@Data
@Slf4j
public class SolarPanel {
    private double efficiency;
    private double capacity;
    private double totalEnergyProduced;
    private String panelId;

    public SolarPanel(double efficiency, double capacity) {
        this.efficiency = efficiency;
        this.capacity = capacity;
        this.totalEnergyProduced = 0.0;
        this.panelId = "SOLAR-" + System.currentTimeMillis();
        log.info("☀️ Solarpanel {} initialisiert - Effizienz: {}%, Kapazität: {} kW",
            panelId, efficiency * 100, capacity);
    }

    public SolarPanel(String panelId, double efficiency, double capacity) {
        this.panelId = panelId;
        this.efficiency = efficiency;
        this.capacity = capacity;
        this.totalEnergyProduced = 0.0;
        log.info("☀️ Solarpanel {} initialisiert - Effizienz: {}%, Kapazität: {} kW",
            panelId, efficiency * 100, capacity);
    }

    /**
     * Absorbiert Sonnenlicht und berechnet Energieproduktion
     */
    public double absorbSunlight() {
        double energyProduced = efficiency * capacity;
        totalEnergyProduced += energyProduced;
        log.info("☀️ Panel {}: Sonnenlicht absorbiert → {} kWh erzeugt (Total: {} kWh)",
            panelId, energyProduced, totalEnergyProduced);
        return energyProduced;
    }

    /**
     * Absorbiert Sonnenlicht mit variabler Sonneneinstrahlung
     */
    public double absorbSunlight(double sunlightIntensity) {
        double energyProduced = efficiency * capacity * sunlightIntensity;
        totalEnergyProduced += energyProduced;
        log.info("☀️ Panel {}: Intensität {}% → {} kWh erzeugt",
            panelId, sunlightIntensity * 100, energyProduced);
        return energyProduced;
    }

    /**
     * Optimiert die Panel-Effizienz
     */
    public void optimizeEfficiency() {
        double oldEfficiency = this.efficiency;
        this.efficiency = Math.min(0.95, this.efficiency * 1.05);
        log.info("🔧 Panel {} Effizienz optimiert: {}% → {}%",
            panelId, oldEfficiency * 100, efficiency * 100);
    }
}
