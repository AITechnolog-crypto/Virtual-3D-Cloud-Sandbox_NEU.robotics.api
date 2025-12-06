package com.june.kisystem;

import java.util.logging.Logger;

public class Battery implements IComponent {
    private final Logger logger = Logger.getLogger(Battery.class.getName());

    @Override
    public void performFunction() {
        logger.info("Batterie speichert Energie.");
    }
}
