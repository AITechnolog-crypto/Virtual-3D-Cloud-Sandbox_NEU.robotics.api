package com.june.controller.api;

import java.util.HashMap;
import java.util.Map;

/**
 * HEALTH MONITORING AI SYSTEM - Simulation only (no external SDKs required)
 */
public class HealthMonitoringAI {
    private String projekt;
    private String standort;
    private String endpunkt;

    public HealthMonitoringAI(String projekt, String standort, String endpunkt) {
        this.projekt = projekt;
        this.standort = standort;
        this.endpunkt = endpunkt;
    }

    public void startMonitoring(HealthMonitoringSession session) {
        try {
            session.addLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            session.addLog("🏥 HEALTH MONITORING GESTARTET");
            session.addLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            // Kontinuierliche Überwachung
            while (session.isActive()) {
                // Sammle Vitaldaten
                Map<String, Object> vitalData = collectVitalData(session);

                // Mache KI-Vorhersage (Simulation)
                Map<String, Object> prediction = makePrediction(vitalData, session);

                // Analysiere Gesundheitszustand
                analyzeHealthStatus(prediction, session);

                // Warte 5 Sekunden bis zur nächsten Messung
                Thread.sleep(5000);
            }

        } catch (Exception e) {
            session.addLog("❌ FEHLER: " + e.getMessage());
            session.setStatus("FEHLER");
        }
    }

    private Map<String, Object> collectVitalData(HealthMonitoringSession session) {
        session.addLog("📊 Sammle Vitaldaten...");

        // Simuliere realistische Vitaldaten
        int heartRate = 60 + (int)(Math.random() * 40);
        String bloodPressure = (100 + (int)(Math.random() * 40)) + "/" + (60 + (int)(Math.random() * 30));
        double temperature = 36.5 + (Math.random() * 1.5);
        int oxygenSaturation = 95 + (int)(Math.random() * 5);
        int respiratoryRate = 12 + (int)(Math.random() * 8);

        session.setHeartRate(heartRate);
        session.setBloodPressure(bloodPressure);
        session.setTemperature(temperature);
        session.setOxygenSaturation(oxygenSaturation);
        session.setRespiratoryRate(respiratoryRate);

        Map<String, Object> vitalData = new HashMap<>();
        vitalData.put("heartRate", heartRate);
        vitalData.put("bloodPressure", bloodPressure);
        vitalData.put("temperature", temperature);
        vitalData.put("oxygenSaturation", oxygenSaturation);
        vitalData.put("respiratoryRate", respiratoryRate);
        vitalData.put("timestamp", System.currentTimeMillis());

        session.addLog("❤️ Herzfrequenz: " + heartRate + " bpm");
        session.addLog("🩸 Blutdruck: " + bloodPressure + " mmHg");
        session.addLog("🌡️ Temperatur: " + String.format("%.1f", temperature) + "°C");
        session.addLog("💨 SpO2: " + oxygenSaturation + "%");
        session.addLog("💨 Atemfrequenz: " + respiratoryRate + "/min");

        return vitalData;
    }

    public Map<String, Object> makePrediction(Map<String, Object> vitalData, HealthMonitoringSession session) {
        session.addLog("🤖 KI analysiert Vitaldaten (Simulation)...");
        session.incrementPredictionCount();

        return simulatePrediction(vitalData, session);
    }

    private Map<String, Object> simulatePrediction(Map<String, Object> vitalData, HealthMonitoringSession session) {
        Map<String, Object> prediction = new HashMap<>();

        // Berechne Gesundheitsscore (0-100)
        int heartRate = (int) vitalData.get("heartRate");
        int oxygenSaturation = (int) vitalData.get("oxygenSaturation");
        double temperature = (double) vitalData.get("temperature");

        double healthScore = 100.0;

        // Herzfrequenz Check (Optimal: 60-100 bpm)
        if (heartRate < 60 || heartRate > 100) healthScore -= 10;
        if (heartRate < 50 || heartRate > 120) healthScore -= 20;

        // Sauerstoffsättigung Check (Optimal: >95%)
        if (oxygenSaturation < 95) healthScore -= 15;
        if (oxygenSaturation < 90) healthScore -= 30;

        // Temperatur Check (Optimal: 36.5-37.5°C)
        if (temperature < 36.0 || temperature > 37.5) healthScore -= 10;
        if (temperature < 35.5 || temperature > 38.0) healthScore -= 25;

        healthScore = Math.max(0, Math.min(100, healthScore));

        session.setHealthScore(healthScore);

        // Risikoanalyse
        String riskLevel;
        String recommendation;

        if (healthScore >= 90) {
            riskLevel = "NIEDRIG";
            recommendation = "Keine Maßnahmen erforderlich. Weiter überwachen.";
            session.addLog("✅ Gesundheitszustand: AUSGEZEICHNET");
        } else if (healthScore >= 70) {
            riskLevel = "MITTEL";
            recommendation = "Leichte Abweichungen erkannt. Engmaschige Überwachung empfohlen.";
            session.addLog("⚠️ Gesundheitszustand: GUT");
        } else if (healthScore >= 50) {
            riskLevel = "ERHÖHT";
            recommendation = "Signifikante Abweichungen. Ärztliche Konsultation empfohlen.";
            session.addLog("⚠️ Gesundheitszustand: AUFFÄLLIG");
        } else {
            riskLevel = "HOCH";
            recommendation = "KRITISCH! Sofortige medizinische Intervention erforderlich!";
            session.addLog("🚨 Gesundheitszustand: KRITISCH");
        }

        prediction.put("source", "Simulation (AI Model)");
        prediction.put("healthScore", healthScore);
        prediction.put("riskLevel", riskLevel);
        prediction.put("recommendation", recommendation);
        prediction.put("confidence", 0.87 + Math.random() * 0.10);
        prediction.put("timestamp", System.currentTimeMillis());

        session.addLog("📊 Health Score: " + String.format("%.1f", healthScore) + "/100");
        session.addLog("🎯 Risiko-Level: " + riskLevel);
        session.addLog("💡 Empfehlung: " + recommendation);

        return prediction;
    }

    private void analyzeHealthStatus(Map<String, Object> prediction, HealthMonitoringSession session) {
        String riskLevel = (String) prediction.get("riskLevel");

        if ("HOCH".equals(riskLevel)) {
            session.addLog("🚨 ALARM: Kritischer Gesundheitszustand erkannt!");
            session.addLog("🚨 Benachrichtige Notfallteam...");
            session.incrementAlerts();
        } else if ("ERHÖHT".equals(riskLevel)) {
            session.addLog("⚠️ Warnung: Erhöhtes Risiko erkannt");
            session.incrementWarnings();
        }

        session.addLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}
