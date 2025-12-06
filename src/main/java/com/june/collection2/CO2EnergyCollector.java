package com.june.collection2;

import java.util.Random;
import java.util.logging.Logger;

public class CO2EnergyCollector implements IEnergyCollector {
    private final Logger logger = Logger.getLogger(CO2EnergyCollector.class.getName());
    private final int collectionRate;

    public CO2EnergyCollector(int collectionRate) {
        this.collectionRate = collectionRate;
    }

    @Override
    public Energy collectEnergy() {
        int collectedAmount = new Random().nextInt(collectionRate);
        logger.info("CO2-Energie gesammelt: " + collectedAmount);
        return new Energy(collectedAmount, "kWh");
    }
}
