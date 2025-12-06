package com.june.simulation.energy;

/**
 * Einfache Energieklasse (Menge + Einheit) für die Simulation.
 */
public class Energy {
    public double amount;
    public String unit;

    public Energy(double amount, String unit) {
        this.amount = amount;
        this.unit = unit == null ? "kWh" : unit;
    }

    @Override
    public String toString() {
        return amount + " " + unit;
    }
}
