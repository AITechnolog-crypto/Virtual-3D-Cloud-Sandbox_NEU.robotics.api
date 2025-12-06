package com.june.collection2;

import java.util.logging.Logger;

public class SatelliteCommunicator implements ISatelliteCommunicator {
    private final Logger logger = Logger.getLogger(SatelliteCommunicator.class.getName());

    @Override
    public void receiveCommands() {
        logger.info("Befehle vom Satelliten empfangen.");
    }
}
