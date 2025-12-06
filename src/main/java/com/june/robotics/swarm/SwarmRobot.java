package com.june.robotics.swarm;

import com.june.energy.solar.SolarPanel;
import com.june.energy.solar.PhotocatalyticConverter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 🤖 Schwarmroboter mit Solar- und CO2-Konversion
 * Kombiniert mehrere Subsysteme für autonome Operation
 */
@Data
@Slf4j
public class SwarmRobot {
    private SolarPanel solarPanel;
    private PhotocatalyticConverter converter;
    private String robotId;
    private boolean ready;
    private double operatingHours;
    private String status;

    public SwarmRobot(SolarPanel solarPanel, PhotocatalyticConverter converter) {
        this.solarPanel = solarPanel;
        this.converter = converter;
        this.robotId = "ROBOT-" + System.currentTimeMillis();
        this.ready = checkReadiness();
        this.operatingHours = 0.0;
        this.status = "INITIALISIERT";

        log.info("🤖 Schwarmroboter {} erstellt", robotId);
        log.info("   Solar: {} (Eff: {}%)", solarPanel.getPanelId(), solarPanel.getEfficiency() * 100);
        log.info("   Konverter: {} (Rate: {}%)", converter.getConverterId(), converter.getConversionRate() * 100);
    }

    public SwarmRobot(String robotId, SolarPanel solarPanel, PhotocatalyticConverter converter) {
        this.robotId = robotId;
        this.solarPanel = solarPanel;
        this.converter = converter;
        this.ready = checkReadiness();
        this.operatingHours = 0.0;
        this.status = "INITIALISIERT";

        log.info("🤖 Schwarmroboter {} erstellt", robotId);
    }

    /**
     * Überprüft, ob der Roboter betriebsbereit ist
     */
    public boolean isReady() {
        ready = checkReadiness();
        return ready;
    }

    private boolean checkReadiness() {
        boolean solarReady = solarPanel != null && solarPanel.getEfficiency() > 0;
        boolean converterReady = converter != null && converter.getConversionRate() > 0;

        if (solarReady && converterReady) {
            status = "BEREIT";
            return true;
        } else {
            status = "NICHT BEREIT";
            return false;
        }
    }

    /**
     * Hauptoperation des Roboters
     */
    public void operate() {
        if (!isReady()) {
            handleError("Roboter ist nicht betriebsbereit!");
            return;
        }

        log.info("🚀 Roboter {} startet Operation", robotId);
        status = "AKTIV";

        try {
            // 1. Energie sammeln
            double energy = solarPanel.absorbSunlight(0.8); // 80% Sonneneinstrahlung
            log.info("⚡ Energie gesammelt: {} kWh", energy);

            // 2. CO2 konvertieren
            double co2ToConvert = energy * 10; // 10 kg CO2 pro kWh
            double chemicals = converter.convertCO2(co2ToConvert);
            log.info("🌿 Chemikalien produziert: {} kg", chemicals);

            // 3. Betriebsstunden erhöhen
            operatingHours += 1.0;

            status = "OPERATION ERFOLGREICH";
            log.info("✅ Roboter {} Operation abgeschlossen ({}h Betriebszeit)", robotId, operatingHours);

        } catch (Exception e) {
            handleError("Fehler während Operation: " + e.getMessage());
        }
    }

    /**
     * Fehlerbehandlung
     */
    public void handleError(String errorMessage) {
        status = "FEHLER";
        log.error("❌ Roboter {}: {}", robotId, errorMessage);
    }

    /**
     * Optimiert alle Subsysteme
     */
    public void optimizeSystems() {
        log.info("🔧 Optimiere Roboter {} Systeme...", robotId);
        solarPanel.optimizeEfficiency();
        converter.optimizeConversion();
        status = "OPTIMIERT";
    }

    /**
     * Status-Report
     */
    public String getStatusReport() {
        return String.format(
            "RobotEntity %s [%s]\n" +
            "  Betriebszeit: %.1f h\n" +
            "  Solar Energie: %.2f kWh\n" +
            "  CO2 Reduktion: %.2f kg\n" +
            "  Chemikalien: %.2f kg",
            robotId, status, operatingHours,
            solarPanel.getTotalEnergyProduced(),
            converter.getCO2Reduction(),
            converter.getTotalChemicalsProduced()
        );
    }
}
