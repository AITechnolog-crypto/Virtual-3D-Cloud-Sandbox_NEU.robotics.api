package de.nursystem.drone;

/**
 * Repräsentiert einen Geometer (Vermessungsingenieur)
 */
class Geometer {
    private final String name;
    private final String lizenzNummer;

    public Geometer(String name) {
        this.name = name;
        this.lizenzNummer = "GEO-" + System.currentTimeMillis();
    }

    public String getName() { return name; }
    public String getLizenzNummer() { return lizenzNummer; }
}
