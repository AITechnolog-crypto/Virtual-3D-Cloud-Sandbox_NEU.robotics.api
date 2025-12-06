package com.june.production.cloud;

/**
 * Verwalter für Cloud-Ressourcen.
 */
public interface ICloudResourceManager {
    void allocateResources() throws ResourceAllocationException;
    void deallocateResources();
}