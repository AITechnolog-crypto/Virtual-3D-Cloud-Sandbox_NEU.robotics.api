package com.june.autonomy;

import com.june.autonomy.action.IAction;
import com.june.autonomy.action.MoveAction;

import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Einfache Implementierung einer autonomen Maschine mit steuerbarer Aggressivität.
 * Fehlerwahrscheinlichkeiten werden basierend auf dem Aggressivitätsgrad simuliert.
 */
public class AutonomousMachine implements IAutonomousMachine {
    private static final Logger logger = Logger.getLogger(AutonomousMachine.class.getName());

    // Aggressivität im Bereich [0, 1]. volatile für Thread-Reads ohne Lock.
    private volatile double aggressiveness = 0.5; // Standardaggressivität

    @Override
    public void performPositiveActions() {
        try {
            logger.info("Starte positive Aktionen.");
            // ... tatsächliche Logik ... (Platzhalter)

            // Zufälliger Fehler basierend auf Aggressivität: je aggressiver, desto höher die Fehlerrate
            double rnd = ThreadLocalRandom.current().nextDouble();
            if (rnd > (1.0 - aggressiveness)) {
                throw new RuntimeException("Fehler bei positiver Aktion!");
            }

            logger.info("Positive Aktionen erfolgreich abgeschlossen.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Fehler bei positiven Aktionen: ", e);
            // ggf. weitere Fehlerbehandlung (Retry/Compensation)
        }
    }

    /**
     * Neue Variante gemäß Spezifikation: führt genau eine Aktion aus, sofern sie nicht negativ ist.
     */
    @Override
    public void performPositiveActions(IAction action) {
        try {
            if (action == null) {
                logger.warning("Keine Aktion übergeben – überspringe.");
                return;
            }
            if (action.isNegative()) {
                logger.warning("Negative Aktion erkannt: " + action.getDescription() + ". Aktion wird nicht ausgeführt.");
                return;
            }
            logger.info("Führe positive Aktion aus: " + action.getDescription());
            // Beispiel: spezielle Logik für MoveAction
            if (action instanceof MoveAction move) {
                // Platzhalter: Hier könnte Bewegung ausgeführt werden
                logger.info("Spezifische MoveAction-Logik: Distanz=" + move.getDistance());
            }

            // Fehlerwahrscheinlichkeit wie in der Alt-API
            double rnd = ThreadLocalRandom.current().nextDouble();
            if (rnd > (1.0 - aggressiveness)) {
                throw new RuntimeException("Fehler bei Aktion: " + action.getDescription());
            }
            logger.info("Aktion erfolgreich abgeschlossen: " + action.getDescription());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Fehler bei der Ausführung der Aktion: " + (action != null ? action.getDescription() : "<null>"), e);
        }
    }

    @Override
    public void preventNegativeActions() {
        try {
            logger.info("Starte Verhinderung negativer Aktionen.");
            // ... tatsächliche Logik ... (Platzhalter)

            // Zufälliger Fehler basierend auf Aggressivität: je aggressiver, desto höher die Fehlerrate
            double rnd = ThreadLocalRandom.current().nextDouble();
            if (rnd < aggressiveness) {
                throw new RuntimeException("Fehler bei der Verhinderung negatiger Aktionen!");
            }

            logger.info("Verhinderung negativer Aktionen erfolgreich abgeschlossen.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Fehler bei der Verhinderung negativer Aktionen: ", e);
            // ggf. weitere Fehlerbehandlung
        }
    }

    @Override
    public void setAggressiveness(double aggressiveness) {
        if (aggressiveness < 0.0 || aggressiveness > 1.0) {
            throw new IllegalArgumentException("Aggressivität muss zwischen 0 und 1 liegen.");
        }
        this.aggressiveness = aggressiveness;
        logger.info("Aggressivität gesetzt auf: " + aggressiveness);
    }
}
