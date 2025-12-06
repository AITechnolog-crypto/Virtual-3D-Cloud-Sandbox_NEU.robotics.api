package com.june.factory;

/**
 * Fabrik-Interface zur Produktion eines Energiespeichers.
 */
public interface IEnergyStorageFactory {
    EnergyStorage produceEnergyStorage() throws ResourceAllocationException, BlockchainTransactionException;
}
