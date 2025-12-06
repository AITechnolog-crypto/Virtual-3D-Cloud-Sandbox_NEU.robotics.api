package com.june.kisystem;

import java.util.logging.Logger;

public class Drone implements IComponent {
    private final Logger logger = Logger.getLogger(Drone.class.getName());

    @Override
    public void performFunction() {
        logger.info("Drohne führt Überwachung durch.");
    }
}
