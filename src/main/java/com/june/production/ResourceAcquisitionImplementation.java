package com.june.production;

import java.util.logging.Logger;

/**
 * Implementierung der Ressourcenbeschaffung.
 */
public class ResourceAcquisitionImplementation implements IResourceAcquisition {
    private static final Logger logger = Logger.getLogger(ResourceAcquisitionImplementation.class.getName());

    @Override
    public void acquireResources(double budget) throws ResourceAcquisitionException {
        double resourceCost = 5000.0; // Beispielwert
        if (budget >= resourceCost) {
            logger.info("Ressourcen im Wert von " + resourceCost + " erworben.");
        } else {
            throw new ResourceAcquisitionException("Nicht genügend Budget für die Ressourcenbeschaffung vorhanden.");
        }
    }
}