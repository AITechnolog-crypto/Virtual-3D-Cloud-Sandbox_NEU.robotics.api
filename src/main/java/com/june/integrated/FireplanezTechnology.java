package com.june.integrated;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Fireplanez-Implementierung der alten Technologie mit robuster Fehlerbehandlung.
 */
public class FireplanezTechnology implements IAncientTechnology {
    private static final Logger logger = Logger.getLogger(FireplanezTechnology.class.getName());

    @Override
    public void performAncientTasks() {
        try {
            logger.info("Ausführung von Aufgaben mit Fireplanez-Technologie.");
            // ... tatsächliche Logik ... (Platzhalter)
            if (Math.random() < 0.3) {
                throw new RuntimeException("Fehler in der Fireplanez Technologie!");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Fehler bei Fireplanez-Aufgaben: ", e);
            // ggf. weitere Fehlerbehandlung
        }
    }
}
