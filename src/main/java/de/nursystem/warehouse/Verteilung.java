package de.nursystem.warehouse;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Verteilung an eine Region
 */
class Verteilung {
    private final String region;
    private final Map<String, Double> produkte;
    private final Date zeitstempel;
    private VerteilungsStatus status;

    public Verteilung(String region) {
        this.region = region;
        this.produkte = new HashMap<>();
        this.zeitstempel = new Date();
        this.status = VerteilungsStatus.IN_BEARBEITUNG;
    }

    public void addProdukt(String name, double menge) {
        produkte.put(name, menge);
    }

    public String getRegion() { return region; }
    public Map<String, Double> getProdukte() { return produkte; }
    public Date getZeitstempel() { return zeitstempel; }
    public VerteilungsStatus getStatus() { return status; }
    public void setStatus(VerteilungsStatus status) { this.status = status; }
}
