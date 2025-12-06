package com.june.integrated;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Einfache Beispiel-Implementierung für alte Technologie.
 */
public class AncientTechnology implements IAncientTechnology {
    private static final Logger logger = Logger.getLogger(AncientTechnology.class.getName());

    @Override
    public void performAncientTasks() throws Exception {
        logger.log(Level.INFO, "Ausführen alter Technologien (Beispiel)");
        // Platzhalter-Logik: z.B. Kalibrierung, Legacy‑I/O, etc.
    }
}
