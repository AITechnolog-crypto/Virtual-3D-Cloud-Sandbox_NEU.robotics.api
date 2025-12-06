package com.june.drone.service;

import com.june.drone.model.Position;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MagnetfeldNavigator {
    public Position bestimmePosition() {
        log.debug("MagnetfeldNavigator: Bestimme Position über Magnetfeld.");
        // Fallback: Magnetfeld-basierte Navigation
        return new Position(
            48.8566 + (Math.random() - 0.5) * 0.05,
            2.3522 + (Math.random() - 0.5) * 0.05
        );
    }
}
