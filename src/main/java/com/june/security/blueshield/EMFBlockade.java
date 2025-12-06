package com.june.security.blueshield;

import lombok.extern.slf4j.Slf4j;

/**
 * ⚡ Elektromagnetische Feld-Blockade
 * Blockiert elektronische Systeme und Kommunikation
 */
@Slf4j
public class EMFBlockade {
    private double power;
    private double range;
    private boolean active;

    public EMFBlockade() {
        this.power = 5000.0; // kW
        this.range = 100.0; // km
        this.active = false;
    }

    /**
     * Aktiviert die EMF-Blockade
     */
    public void activate() {
        log.info("⚡ Aktiviere elektromagnetische Blockade...");
        log.info("🔋 Leistung: {} kW", power);
        log.info("📡 Reichweite: {} km", range);

        disruptCommunications();
        disableElectronics();

        this.active = true;
        log.info("✅ EMF-Blockade erfolgreich aktiviert!");
    }

    private void disruptCommunications() {
        log.info("📵 Störe Kommunikationssysteme...");
    }

    private void disableElectronics() {
        log.info("💻 Deaktiviere elektronische Systeme...");
    }

    public boolean isActive() {
        return active;
    }
}
