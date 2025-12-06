package com.june.collection2;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AutonomousEnergyCollectionSystem {
    private final Logger logger = Logger.getLogger(AutonomousEnergyCollectionSystem.class.getName());
    private final List<IEnergyCollector> energyCollectors;
    private final IEnergyStorage energyStorage;
    private final ISatelliteCommunicator satelliteCommunicator;

    public AutonomousEnergyCollectionSystem(IEnergyStorage energyStorage, ISatelliteCommunicator satelliteCommunicator) {
        this.energyCollectors = new ArrayList<>();
        this.energyStorage = energyStorage;
        this.satelliteCommunicator = satelliteCommunicator;
    }

    public void addEnergyCollector(IEnergyCollector collector) {
        energyCollectors.add(collector);
    }

    public void collectAndStoreEnergy() {
        try {
            satelliteCommunicator.receiveCommands();
            for (IEnergyCollector collector : energyCollectors) {
                Energy collectedEnergy = collector.collectEnergy();
                energyStorage.storeEnergy(collectedEnergy);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Fehler beim Sammeln und Speichern von Energie", e);
        }
    }

    public static void main(String[] args) {
        IEnergyStorage energyStorage = new EnergyStorage();
        ISatelliteCommunicator satelliteCommunicator = new SatelliteCommunicator();
        AutonomousEnergyCollectionSystem energySystem = new AutonomousEnergyCollectionSystem(energyStorage, satelliteCommunicator);

        energySystem.addEnergyCollector(new SolarEnergyCollector(100));
        energySystem.addEnergyCollector(new CO2EnergyCollector(50));

        energySystem.collectAndStoreEnergy();
        energySystem.collectAndStoreEnergy();
    }
}
