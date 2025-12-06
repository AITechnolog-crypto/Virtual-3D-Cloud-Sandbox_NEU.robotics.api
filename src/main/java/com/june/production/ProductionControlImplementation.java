package com.june.production;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementierung der Produktionssteuerung.
 */
public class ProductionControlImplementation implements IProductionControl {
    private static final Logger logger = Logger.getLogger(ProductionControlImplementation.class.getName());

    @Override
    public void startProduction() throws ProductionStartException {
        logger.info("Produktion gestartet.");
        try {
            Thread.sleep(2000); // Simulierte Produktionszeit
            logger.info("Produktion abgeschlossen.");
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "Produktion unterbrochen.", e);
            Thread.currentThread().interrupt();
            throw new ProductionStartException("Fehler beim Start der Produktion.", e);
        }
    }
}