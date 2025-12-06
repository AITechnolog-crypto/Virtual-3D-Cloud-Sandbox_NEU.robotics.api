package com.june.agro;

/**
 * Klasse für landwirtschaftliche Produkte.
 */
public class LandwirtschaftsProdukt {
    private String name;
    private double menge;

    public LandwirtschaftsProdukt(String name, double menge) {
        this.name = name;
        this.menge = menge;
    }

    public String getName() {
        return name;
    }

    public double getMenge() {
        return menge;
    }

    public void setMenge(double menge) {
        this.menge = menge;
    }

    @Override
    public String toString() {
        return String.format("%s: %.2f Einheiten", name, menge);
    }
}
