package com.june.warehouse;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Autonomes Lagerbausystem: nutzt einen WarehouseBuilder und behandelt Fehler robust.
 */
public class AutonomousWarehouseSystem {
    private static final Logger LOGGER = Logger.getLogger(AutonomousWarehouseSystem.class.getName());
    private final IWarehouseBuilder warehouseBuilder;

    public AutonomousWarehouseSystem(IWarehouseBuilder warehouseBuilder) {
        this.warehouseBuilder = warehouseBuilder;
    }

    public void initiateWarehouseConstruction(Location location) {
        try {
            warehouseBuilder.buildWarehouse(location);
        } catch (ConstructionFailedException e) {
            LOGGER.log(Level.SEVERE, "Lagerbau fehlgeschlagen: " + e.getMessage(), e);
            // Hier könnte eine Logik zur Fehlerbehebung implementiert werden.
        }
    }
}
