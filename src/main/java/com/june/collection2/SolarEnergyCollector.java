package com.june.collection2;

import java.util.Random;
import java.util.logging.Logger;

public class SolarEnergyCollector implements IEnergyCollector {
    private final Logger logger = Logger.getLogger(SolarEnergyCollector.class.getName());
    private final int collectionRate;

    public SolarEnergyCollector(int collectionRate) {
        this.collectionRate = collectionRate;
    }

    @Override
    public Energy collectEnergy() {
        int collectedAmount = new Random().nextInt(collectionRate);
        logger.info("Solarenergie gesammelt: " + collectedAmount);
        return new Energy(collectedAmount, "kWh");
    }
}
