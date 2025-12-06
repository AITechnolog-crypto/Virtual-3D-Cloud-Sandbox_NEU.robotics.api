package com.june.service.drone;

import com.june.model.drone.Position;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MagnetfeldNavigator {
    public Position bestimmePosition() {
        log.info("Magnetfeld-Navigator: Bestimme Position (Fallback).");
        // Fallback: Magnetfeld-basierte Navigation
        Position p = new Position(
            48.8566 + (Math.random() - 0.5) * 0.05,
            2.3522 + (Math.random() - 0.5) * 0.05
        );
        log.info("Magnetfeld-Navigator: Position bestimmt: Lat={}, Lng={}", p.latitude, p.longitude);
        return p;
    }
}
