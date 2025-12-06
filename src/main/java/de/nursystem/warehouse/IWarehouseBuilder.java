package com.june.warehouse;

/**
 * Interface für den Bau von Lagern.
 */
public interface IWarehouseBuilder {
    void buildWarehouse(Location location) throws ConstructionFailedException;
}
