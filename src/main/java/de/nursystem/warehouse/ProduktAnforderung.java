package de.nursystem.warehouse;

/**
 * Produkt-Anforderung für Verteilung
 */
class ProduktAnforderung {
    private final String produktName;
    private final double menge;

    public ProduktAnforderung(String produktName, double menge) {
        this.produktName = produktName;
        this.menge = menge;
    }

    public String getProduktName() { return produktName; }
    public double getMenge() { return menge; }
}
