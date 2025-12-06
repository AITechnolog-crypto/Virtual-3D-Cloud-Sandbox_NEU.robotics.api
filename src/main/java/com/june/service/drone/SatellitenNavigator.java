package com.june.service.drone;

import com.june.model.drone.Position;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SatellitenNavigator {
    public Position lokalisieren() {
        log.info("Satelliten-Navigator: Versuche GPS-Lokalisierung.");
        // Simuliere GPS-Lokalisierung
        if (Math.random() > 0.1) { // 90% Erfolgsrate
            Position p = new Position(
                48.8566 + (Math.random() - 0.5) * 0.1,
                2.3522 + (Math.random() - 0.5) * 0.1
            );
            log.info("Satelliten-Navigator: GPS-Lokalisierung erfolgreich: Lat={}, Lng={}", p.latitude, p.longitude);
            return p;
        }
        log.warn("Satelliten-Navigator: GPS-Signal schwach oder nicht verfügbar.");
        return null;
    }
}
