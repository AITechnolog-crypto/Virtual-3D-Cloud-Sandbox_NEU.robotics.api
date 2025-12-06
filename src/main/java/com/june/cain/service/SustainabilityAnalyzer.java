package com.june.cain.service;

import com.june.cain.model.CainSession;
import com.june.cain.model.SatelliteData;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SustainabilityAnalyzer {

    public void integrateSatelliteData(SatelliteData data, CainSession session) throws InterruptedException {
        log.info("Integriere Satellitendaten für Nachhaltigkeitsanalyse");

        session.addLog("  ↳ Nachhaltigkeits-Score berechnen...");
        double sustainabilityScore = calculateSustainabilityScore(data, session);
        session.setSustainabilityScore(sustainabilityScore);
        Thread.sleep(600);

        session.addLog("  ↳ Ökologische Trends identifizieren...");
        identifyEcologicalTrends(data, session);
        Thread.sleep(600);

        session.addLog("  ↳ Empfehlungen generieren...");
        generateRecommendations(data, session);
        Thread.sleep(600);

        session.incrementSustainabilityAnalyses();
        log.info("Nachhaltigkeitsanalyse erfolgreich abgeschlossen");
    }

    private double calculateSustainabilityScore(SatelliteData data, CainSession session) {
        double score = 0;
        score += data.getNdvi() * 30; // Max 27 Punkte
        score += (data.getWaterQuality() / 10.0) * 30; // Max 30 Punkte
        score += (25.0 - data.getAirQuality()) / 25.0 * 25; // Max 25 Punkte
        score += (1.5 - Math.abs(data.getTempAnomaly())) / 1.5 * 15; // Max 15 Punkte

        score = Math.max(0, Math.min(100, score));
        session.addLog("     • Score: " + String.format("%.1f", score) + "/100");
        return score;
    }

    private void identifyEcologicalTrends(SatelliteData data, CainSession session) {
        session.addLog("     • Trend: " + (data.getNdvi() > 0.5 ? "Positive Vegetationsentwicklung" : "Überwachung erforderlich"));
    }

    private void generateRecommendations(SatelliteData data, CainSession session) {
        if (data.getNdvi() < 0.4) {
            session.addLog("     • Empfehlung: Aufforstungsmaßnahmen erwägen");
        }
        if (data.getWaterQuality() < 7.5) {
            session.addLog("     • Empfehlung: Gewässerschutzmaßnahmen verstärken");
        }
        if (data.getAirQuality() > 15) {
            session.addLog("     • Empfehlung: Emissionsreduktion priorisieren");
        }
    }
}
