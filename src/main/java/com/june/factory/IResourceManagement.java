package com.june.factory;

/**
 * Schnittstelle für die Ressourcenverwaltung (Fabrik-Kontext).
 */
public interface IResourceManagement {
    void allocateResourcesForGood() throws ResourceAllocationException;
}
