package com.june.drone.service;

import com.june.drone.model.DrohnenSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FlugbahnKorrektor {
    public void korrigieren(DrohnenSession session) {
        log.debug("FlugbahnKorrektor: Korrigiere Flugbahn für Drohne {}.", session.getDroneId());
        // Simuliere Windkompensation
        double windSpeed = 5 + Math.random() * 15;
        session.setWindSpeed(windSpeed);

        if (windSpeed > 15) {
            session.addLog("💨 Starker Wind: " + String.format("%.1f", windSpeed) + " km/h - Korrektur aktiv");
            log.info("FlugbahnKorrektor: Starker Wind erkannt ({} km/h) für Drohne {}. Korrektur aktiv.", String.format("%.1f", windSpeed), session.getDroneId());
        }
    }
}
