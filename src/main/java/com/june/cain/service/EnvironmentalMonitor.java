package com.june.cain.service;

import com.june.cain.model.CainSession;
import com.june.cain.model.SatelliteData;
import lombok.extern.slf44j.Slf4j;

@Slf4j
public class EnvironmentalMonitor {

    public void updateWithSatelliteData(SatelliteData data, CainSession session) throws InterruptedException {
        log.info("Aktualisiere Umweltüberwachung mit Satellitendaten");

        session.addLog("  ↳ Vegetationsanalyse durchführen...");
        analyzeVegetation(data, session);
        Thread.sleep(600);

        session.addLog("  ↳ Gewässerüberwachung durchführen...");
        analyzeWater(data, session);
        Thread.sleep(600);

        session.addLog("  ↳ Luftqualität bewerten...");
        analyzeAirQuality(data, session);
        Thread.sleep(600);

        session.addLog("  ↳ Temperatur-Trends analysieren...");
        analyzeTemperature(data, session);

        session.incrementEnvironmentalUpdates();
        log.info("Umweltüberwachung erfolgreich aktualisiert");
    }

    private void analyzeVegetation(SatelliteData data, CainSession session) {
        String status = data.getNdvi() > 0.6 ? "AUSGEZEICHNET" :
                       data.getNdvi() > 0.4 ? "GUT" : "BEDENKLICH";
        session.addLog("     • Vegetation: " + status);
    }

    private void analyzeWater(SatelliteData data, CainSession session) {
        String status = data.getWaterQuality() > 8.5 ? "SEHR GUT" :
                       data.getWaterQuality() > 7.5 ? "GUT" : "BEFRIEDIGEND";
        session.addLog("     • Wasserqualität: " + status);
    }

    private void analyzeAirQuality(SatelliteData data, CainSession session) {
        String status = data.getAirQuality() < 10 ? "SEHR GUT" :
                       data.getAirQuality() < 20 ? "GUT" : "MÄSSIG";
        session.addLog("     • Luftqualität: " + status);
    }

    private void analyzeTemperature(SatelliteData data, CainSession session) {
        String status = Math.abs(data.getTempAnomaly()) < 0.5 ? "NORMAL" :
                       Math.abs(data.getTempAnomaly()) < 1.5 ? "LEICHT ERHÖHT" : "ERHÖHT";
        session.addLog("     • Temperatur-Anomalie: " + status);
    }
}
