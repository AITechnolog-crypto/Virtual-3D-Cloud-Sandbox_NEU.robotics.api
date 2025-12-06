package com.june.production;

/**
 * Startet die Produktion.
 */
public interface IProductionControl {
    void startProduction() throws ProductionStartException;
}