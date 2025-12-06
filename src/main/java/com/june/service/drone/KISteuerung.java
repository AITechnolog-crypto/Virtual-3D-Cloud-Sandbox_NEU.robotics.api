package com.june.service.drone;

import com.june.model.drone.DrohnenSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KISteuerung {
    public String entscheiden(DrohnenSession session) {
        double battery = session.getBattery();
        double altitude = (Double) session.getPosition().getOrDefault("altitude", 0.0);
        double windSpeed = session.getWindSpeed();

        if (battery < 15) {
            log.warn("KISteuerung: Batterie niedrig ({}%). Empfehle Rückkehr zur Basis.", battery);
            return "Rückkehr zur Basis wegen niedrigem Batteriestand";
        }

        if (windSpeed > 20) {
            log.warn("KISteuerung: Starker Wind ({} km/h). Empfehle Höhe anpassen.", windSpeed);
            return "Höhe anpassen wegen starkem Wind";
        }

        if (altitude < 50) {
            log.info("KISteuerung: Höhe niedrig ({}m). Empfehle Höhe erhöhen.", altitude);
            return "Höhe erhöhen für bessere Sicht";
        }
        log.info("KISteuerung: Optimale Bedingungen. Route halten.");
        return "Route halten, alles optimal";
    }
}
