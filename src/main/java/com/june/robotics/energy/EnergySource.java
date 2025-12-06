package com.june.robotics.energy;

import lombok.extern.slf4j.Slf4j;

/**
 * 🌞 Energiequelle für Schwarmroboter
 * Simuliert verschiedene Energiequellen (Solar, Wind, etc.)
 */
@Slf4j
public class EnergySource {
    private String sourceType;
    private double maxOutput;

    public EnergySource(String sourceType, double maxOutput) {
        this.sourceType = sourceType;
        this.maxOutput = maxOutput;
    }

    /**
     * Methode zum Absorbieren von Energie aus der Umgebung
     */
    public double absorb() {
        // Realistischere Logik zur Energieabsorption
        double efficiency = 0.7 + (Math.random() * 0.3); // 70-100% Effizienz
        double absorbedEnergy = maxOutput * efficiency;

        log.debug("⚡ Energiequelle {}: {} kWh absorbiert (Effizienz: {}%)",
            sourceType, absorbedEnergy, efficiency * 100);

        return absorbedEnergy;
    }

    public String getSourceType() {
        return sourceType;
    }
}
