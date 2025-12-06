package com.june.production.cloud;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dummy-Technologieintegrator.
 */
public class TechnologyIntegrator implements ITechnologyIntegrator {
    private static final Logger logger = Logger.getLogger(TechnologyIntegrator.class.getName());

    @Override
    public void integrateTechnology() throws IntegrationFailedException {
        try {
            logger.info("Datenbank und Monitoring in die Cloud integriert.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Fehler bei der Technologieintegration: " + e.getMessage(), e);
            throw new IntegrationFailedException("Technologieintegration fehlgeschlagen.", e);
        }
    }
}