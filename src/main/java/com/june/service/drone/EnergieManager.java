package com.june.service.drone;

import com.june.model.drone.DrohnenSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EnergieManager {
    public void manage(DrohnenSession session) {
        // Batterie-Verbrauch
        double battery = session.getBattery();
        battery -= 0.2;

        // Solar-Aufladung
        double solarPower = Math.random() * 50;
        session.setSolarPower(solarPower);

        if (solarPower > 30) {
            battery += 0.15;
        }

        session.setBattery(Math.max(0, Math.min(100, battery)));

        if (battery < 20) {
            session.addLog("⚠️ Batterie niedrig: " + String.format("%.1f", battery) + "%");
            log.warn("EnergieManager: Batterie niedrig für Drohne {}: {}%", session.getDroneId().substring(0,8), battery);
        }
        log.debug("EnergieManager: Batterie: {}%, Solar: {}W", session.getBattery(), session.getSolarPower());
    }
}
