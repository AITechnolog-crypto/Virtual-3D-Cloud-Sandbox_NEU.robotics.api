package com.june.drone.service;

import com.june.drone.model.DrohnenSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnergieManager {
    public void manage(DrohnenSession session) {
        log.debug("EnergieManager: Verwalte Energie für Drohne {}.", session.getDroneId());
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
            log.warn("EnergieManager: Batterie niedrig ({}%) für Drohne {}.", String.format("%.1f", battery), session.getDroneId());
        }
    }
}
