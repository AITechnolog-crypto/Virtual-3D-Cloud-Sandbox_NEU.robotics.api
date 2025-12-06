package com.june.service.drone;

import com.june.model.drone.DrohnenSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FlugbahnKorrektor {
    public void korrigieren(DrohnenSession session) {
        // Simuliere Windkompensation
        double windSpeed = 5 + Math.random() * 15;
        session.setWindSpeed(windSpeed);
        log.debug("Flugbahn-Korrektor: Windgeschwindigkeit: {} km/h.", windSpeed);

        if (windSpeed > 15) {
            session.addLog("💨 Starker Wind: " + String.format("%.1f", windSpeed) + " km/h - Korrektur aktiv");
            log.warn("Flugbahn-Korrektor: Starker Wind erkannt ({} km/h). Korrektur aktiv.", windSpeed);
        }
    }
}
