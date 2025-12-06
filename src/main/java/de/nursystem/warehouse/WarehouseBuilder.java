package com.june.warehouse;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementierung des Lagerbauers mit DI der Ressourcenverwaltung.
 */
public class WarehouseBuilder implements IWarehouseBuilder {
    private final Logger logger = Logger.getLogger(WarehouseBuilder.class.getName());
    private final IResourceManagement resourceManagement; // Dependency Injection

    public WarehouseBuilder(IResourceManagement resourceManagement) {
        this.resourceManagement = resourceManagement;
    }

    @Override
    public void buildWarehouse(Location location) throws ConstructionFailedException {
        try {
            resourceManagement.allocateResourcesForGood();
            // Hier würde die Logik zur Konstruktion des Lagers implementiert werden.
            logger.info("Lager erfolgreich gebaut bei: " + location);
        } catch (ResourceAllocationException e) {
            logger.log(Level.SEVERE, "Fehler bei der Ressourcenallokation: " + e.getMessage(), e);
            throw new ConstructionFailedException("Fehler beim Bau des Lagers aufgrund von Ressourcenmangel.", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unerwarteter Fehler beim Bau des Lagers: " + e.getMessage(), e);
            throw new ConstructionFailedException("Unerwarteter Fehler beim Bau des Lagers.", e);
        }
    }
}
