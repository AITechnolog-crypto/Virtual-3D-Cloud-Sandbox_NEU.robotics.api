package com.june.collection2;

/**
 * Einfache Energieklasse (Menge + Einheit) für das Sammlungssystem.
 */
public class Energy {
    public double amount;
    public String unit;

    public Energy(double amount, String unit) {
        this.amount = amount;
        this.unit = unit == null ? "kWh" : unit;
    }
}
