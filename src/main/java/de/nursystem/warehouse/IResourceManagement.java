package com.june.warehouse;

/**
 * Interface für die Ressourcenverwaltung.
 */
public interface IResourceManagement {
    void allocateResourcesForGood() throws ResourceAllocationException;
}
