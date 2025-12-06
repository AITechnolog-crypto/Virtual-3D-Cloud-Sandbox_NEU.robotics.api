package com.june.production;

/**
 * Beschafft Ressourcen bis zur Budgetgrenze.
 */
public interface IResourceAcquisition {
    void acquireResources(double budget) throws ResourceAcquisitionException;
}