package de.nursystem.drone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Repräsentiert eine Vermessungsdrohne
 */
class Drohne {
    private static final Logger logger = LoggerFactory.getLogger(Drohne.class);

    private final String id;
    private Punkt startpunkt;
    private Geometer geometer;
    private double batterie = 100.0;

    public Drohne(String id) {
        this.id = id;
    }

    public void setzeStartpunkt(Punkt punkt) {
        this.startpunkt = punkt;
        logger.debug("  📍 Startpunkt gesetzt: {}", punkt);
    }

    public void setzeGeometer(Geometer geometer) {
        this.geometer = geometer;
        logger.debug("  👷 Geometer zugewiesen: {}", geometer.getName());
    }

    public Flugdaten fuehreVermessungsflugDurch() {
        logger.info("  🚁 Starte Vermessungsflug...");

        List<Punkt> messpunkte = new ArrayList<>();
        messpunkte.add(startpunkt);
        messpunkte.add(new Punkt(startpunkt.getX() + 100, startpunkt.getY(), startpunkt.getZ()));
        messpunkte.add(new Punkt(startpunkt.getX() + 100, startpunkt.getY() + 150, startpunkt.getZ()));
        messpunkte.add(new Punkt(startpunkt.getX(), startpunkt.getY() + 150, startpunkt.getZ()));

        batterie -= 35.0; // Batterieabnahme während des Fluges
        logger.info("  🔋 Batteriestatus: {}%", batterie);

        return new Flugdaten(messpunkte, geometer);
    }

    public String getId() { return id; }
}
