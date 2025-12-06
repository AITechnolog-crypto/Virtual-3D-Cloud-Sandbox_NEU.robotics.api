package com.june.warehouse;

/**
 * Hauptprogramm (Demo): startet den Lagerbau an zwei Standorten.
 */
public class Main {
    public static void main(String[] args) {
        IResourceManagement resourceManagement = new ResourceManagement();
        IWarehouseBuilder warehouseBuilder = new WarehouseBuilder(resourceManagement);
        AutonomousWarehouseSystem warehouseSystem = new AutonomousWarehouseSystem(warehouseBuilder);

        Location location = new Location("Antarktis", -75.250973, 0.071389);
        warehouseSystem.initiateWarehouseConstruction(location);

        Location location2 = new Location("Mond", 0, 0);
        warehouseSystem.initiateWarehouseConstruction(location2);
    }
}
