package com.june.drone.service;

import com.june.drone.model.Position;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SatellitenNavigator {
    public Position lokalisieren() {
        log.debug("SatellitenNavigator: Lokalisierung wird durchgeführt.");
        // Simuliere GPS-Lokalisierung
        if (Math.random() > 0.1) { // 90% Erfolgsrate
            return new Position(
                48.8566 + (Math.random() - 0.5) * 0.1,
                2.3522 + (Math.random() - 0.5) * 0.1
            );
        }
        log.warn("SatellitenNavigator: GPS-Signal schwach oder nicht verfügbar.");
        return null;
    }
}
