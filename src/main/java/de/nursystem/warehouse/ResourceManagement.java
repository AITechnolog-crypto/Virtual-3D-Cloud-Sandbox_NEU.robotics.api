package com.june.warehouse;

import java.util.logging.Logger;

/**
 * Implementierung der Ressourcenverwaltung.
 */
public class ResourceManagement implements IResourceManagement {
    private final Logger logger = Logger.getLogger(ResourceManagement.class.getName());

    @Override
    public void allocateResourcesForGood() throws ResourceAllocationException {
        // Logik für die Ressourcenallokation
        logger.info("Ressourcen für den Lagerbau zugewiesen.");
        // Hier könnte eine tatsächliche Ressourcenprüfung und -reservierung stattfinden.
        // Simulieren einer Exception für Demozwecke.
        // throw new ResourceAllocationException("Nicht genügend Ressourcen verfügbar.");
    }
}
