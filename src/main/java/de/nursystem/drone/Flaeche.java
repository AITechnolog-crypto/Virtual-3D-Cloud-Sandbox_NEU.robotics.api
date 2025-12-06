package de.nursystem.drone;

/**
 * Repräsentiert eine vermessene Fläche
 */
class Flaeche {
    private final double flaeche;
    private final String besitzer;
    private final String koordinaten;

    public Flaeche(double flaeche, String besitzer, String koordinaten) {
        this.flaeche = flaeche;
        this.besitzer = besitzer;
        this.koordinaten = koordinaten;
    }

    public double getFlaeche() { return flaeche; }
    public String getBesitzer() { return besitzer; }
    public String getKoordinaten() { return koordinaten; }
}
