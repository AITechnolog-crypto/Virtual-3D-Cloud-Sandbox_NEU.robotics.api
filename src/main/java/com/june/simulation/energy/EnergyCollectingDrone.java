package com.june.simulation.energy;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Energie-sammelnde Drohne: setzt Drohnen ein und speichert gesammelte Energie
 * in einem bereitgestellten Energiespeicher.
 */
public class EnergyCollectingDrone extends AutonomousDroneSystem {
    private static final Logger logger = Logger.getLogger(EnergyCollectingDrone.class.getName());
    private final IEnergyStorageV2 energyStorage;

    public EnergyCollectingDrone(IResourceManagement resourceManagement, IEnergyStorageV2 energyStorage) {
        super(resourceManagement);
        this.energyStorage = energyStorage;
    }

    @Override
    public void deployDronesForGood() {
        super.deployDronesForGood();
        collectAndStoreEnergy();
    }

    void collectAndStoreEnergy() {
        try {
            // Beispielhaftes Sammeln von Energie (Dummy-Implementierung)
            Energy collectedEnergy = new Energy(50, "kWh");
            if (energyStorage.storeEnergy(collectedEnergy)) {
                logger.info("Energie erfolgreich gespeichert.");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Fehler beim Sammeln oder Speichern von Energie", e);
        }
    }

    public static void main(String[] args) {
        IEnergyStorageV2 storage = new AntarcticEnergyStorage(1_000);
        IResourceManagement resourceManagement = new ResourceManagement();
        EnergyCollectingDrone drone = new EnergyCollectingDrone(resourceManagement, storage);
        drone.deployDronesForGood();

        // Zusätzliche Sammlungen gemäß Beispiel
        drone.collectAndStoreEnergy();
        drone.collectAndStoreEnergy();
        drone.collectAndStoreEnergy();
    }
}
