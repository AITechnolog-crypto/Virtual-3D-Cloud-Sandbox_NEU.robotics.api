package com.june.kisystem;

import java.util.logging.Logger;

public class SolarPanel implements IComponent {
    private final Logger logger = Logger.getLogger(SolarPanel.class.getName());

    @Override
    public void performFunction() {
        logger.info("SolarPanel sammelt Sonnenenergie.");
    }
}
