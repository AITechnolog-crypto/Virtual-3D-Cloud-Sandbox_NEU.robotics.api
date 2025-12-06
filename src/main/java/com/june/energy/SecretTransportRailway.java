package com.june.energy;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Geheime Transportbahn. Notfall-Checks sind hier nur simuliert.
 */
public class SecretTransportRailway {
    /**
     * Dummy-Check auf Notfälle. 1% Wahrscheinlichkeit für true, um Verhalten zu demonstrieren.
     */
    public boolean checkForEmergencies() {
        return ThreadLocalRandom.current().nextInt(100) == 0; // ~1%
    }

    public void activate() {
        System.out.println("Die geheime Transportbahn wurde aktiviert.");
    }
}
