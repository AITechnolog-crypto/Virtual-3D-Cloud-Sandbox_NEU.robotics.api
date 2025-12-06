package com.june.factory;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Konkrete Fabrik, die Ressourcen allokiert, eine Blockchain-Transaktion ausführt
 * und anschließend einen Energiespeicher erzeugt.
 */
public class EnergyStorageFactory implements IEnergyStorageFactory {
    private final Logger logger = Logger.getLogger(EnergyStorageFactory.class.getName());
    private final IResourceManagement resourceManagement;
    private final IBlockchainWallet blockchainWallet;

    public EnergyStorageFactory(IResourceManagement resourceManagement, IBlockchainWallet blockchainWallet) {
        this.resourceManagement = resourceManagement;
        this.blockchainWallet = blockchainWallet;
    }

    @Override
    public EnergyStorage produceEnergyStorage() throws ResourceAllocationException, BlockchainTransactionException {
        try {
            resourceManagement.allocateResourcesForGood();
            blockchainWallet.processTransaction(new Transaction("Produktion", "EnergyStorage"));
            EnergyStorage storage = new EnergyStorage("AntarcticModule", 1000);
            logger.info("Energiespeicher produziert: " + storage);
            return storage;
        } catch (ResourceAllocationException | BlockchainTransactionException e) {
            logger.log(Level.SEVERE, "Fehler bei der Produktion des Energiespeichers: " + e.getMessage(), e);
            throw e; // Exception weiterwerfen
        }
    }
}
