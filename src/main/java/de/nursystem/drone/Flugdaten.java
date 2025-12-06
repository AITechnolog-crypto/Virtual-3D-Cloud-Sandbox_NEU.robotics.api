package de.nursystem.drone;

import java.util.List;

/**
 * Enthält die gesammelten Flugdaten
 */
class Flugdaten {
    private final List<Punkt> messpunkte;
    private final Geometer geometer;
    private final long zeitstempel;

    public Flugdaten(List<Punkt> messpunkte, Geometer geometer) {
        this.messpunkte = messpunkte;
        this.geometer = geometer;
        this.zeitstempel = System.currentTimeMillis();
    }

    public List<Punkt> getMesspunkte() { return messpunkte; }
    public Geometer getGeometer() { return geometer; }
    public long getZeitstempel() { return zeitstempel; }
}
