package com.june.finance;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResourceExtractionSystem implements IResourceExtractionSystem {
    private final Logger logger = Logger.getLogger(ResourceExtractionSystem.class.getName());
    private final String resourceType;

    public ResourceExtractionSystem(String resourceType) {
        this.resourceType = resourceType;
    }

    @Override
    public int extractAndProcessResources() throws ResourceExtractionException {
        try {
            int extractedAmount = new Random().nextInt(100);
            logger.info(extractedAmount + " Einheiten von " + resourceType + " wurden extrahiert und verarbeitet.");
            return extractedAmount;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Fehler bei der Ressourcenextraktion", e);
            throw new ResourceExtractionException("Fehler bei der Extraktion von " + resourceType, e);
            
        }
    }
}
