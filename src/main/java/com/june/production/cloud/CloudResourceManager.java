package com.june.production.cloud;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simpler Ressourcenmanager: weist eine feste Anzahl Ressourcen zu und führt Buch.
 */
public class CloudResourceManager implements ICloudResourceManager {
    private static final Logger logger = Logger.getLogger(CloudResourceManager.class.getName());
    private int allocatedResources = 0;

    @Override
    public void allocateResources() throws ResourceAllocationException {
        try {
            int requestedResources = 10; // Beispielwert
            allocatedResources += requestedResources;
            logger.info(requestedResources + " Ressourcen zugewiesen. Aktuell zugewiesene Ressourcen: " + allocatedResources);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Fehler bei der Ressourcenzuweisung: " + e.getMessage(), e);
            throw new ResourceAllocationException("Ressourcenzuweisung fehlgeschlagen.", e);
        }
    }

    @Override
    public void deallocateResources() {
        if (allocatedResources > 0) {
            allocatedResources = 0;
            logger.info("Alle Ressourcen freigegeben.");
        } else {
            logger.info("Keine Ressourcen zum Freigeben vorhanden.");
        }
    }
}