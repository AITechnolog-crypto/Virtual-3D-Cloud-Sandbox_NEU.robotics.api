package com.june.factory;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Hauptsystem: nutzt die EnergyStorageFactory und behandelt Fehlerpfade getrennt.
 */
public class AutonomousEnergyStorageSystem {
    private final Logger logger = Logger.getLogger(AutonomousEnergyStorageSystem.class.getName());
    private final IEnergyStorageFactory energyStorageFactory;

    public AutonomousEnergyStorageSystem(IEnergyStorageFactory energyStorageFactory) {
        this.energyStorageFactory = energyStorageFactory;
    }

    public void startEnergyStorageProduction() {
        try {
            EnergyStorage storage = energyStorageFactory.produceEnergyStorage();
            logger.info("Energiespeicher erfolgreich produziert und im System registriert: " + storage);
            // Weitere Logik zur Verteilung und Installation der Energiespeicher
        } catch (ResourceAllocationException e) {
            logger.log(Level.SEVERE, "Fehler bei der Ressourcenallokation: " + e.getMessage(), e);
        } catch (BlockchainTransactionException e) {
            logger.log(Level.SEVERE, "Fehler bei der Blockchain-Transaktion: " + e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        IResourceManagement resourceManagement = new ResourceManagement();
        IBlockchainWallet blockchainWallet = new BlockchainWallet();
        IEnergyStorageFactory factory = new EnergyStorageFactory(resourceManagement, blockchainWallet);
        AutonomousEnergyStorageSystem system = new AutonomousEnergyStorageSystem(factory);
        system.startEnergyStorageProduction();
    }
}
