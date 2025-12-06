package com.june.drone.service;

import com.june.drone.model.DrohnenSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KISteuerung {
    public String entscheiden(DrohnenSession session) {
        log.debug("KISteuerung: Treffe Entscheidung für Drohne {}.", session.getDroneId());
        double battery = session.getBattery();
        double altitude = (Double) session.getPosition().getOrDefault("altitude", 0.0);
        double windSpeed = session.getWindSpeed();

        if (battery < 15) {
            log.info("KISteuerung: Entscheidung für Drohne {}: Rückkehr zur Basis wegen niedrigem Batteriestand.", session.getDroneId());
            return "Rückkehr zur Basis wegen niedrigem Batteriestand";
        }

        if (windSpeed > 20) {
            log.info("KISteuerung: Entscheidung für Drohne {}: Höhe anpassen wegen starkem Wind.", session.getDroneId());
            return "Höhe anpassen wegen starkem Wind";
        }

        if (altitude < 50) {
            log.info("KISteuerung: Entscheidung für Drohne {}: Höhe erhöhen für bessere Sicht.", session.getDroneId());
            return "Höhe erhöhen für bessere Sicht";
        }
        log.info("KISteuerung: Entscheidung für Drohne {}: Route halten, alles optimal.", session.getDroneId());
        return "Route halten, alles optimal";
    }
}
