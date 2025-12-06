package com.june.production;

/**
 * Datenklasse: Ressourcendatensatz für ML-Training/Vorhersage.
 */
public class ResourceData {
    private String type;
    private double amount;

    public ResourceData(String type, double amount) {
        this.type = type;
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "ResourceData{" +
                "type='" + type + '\'' +
                ", amount=" + amount +
                '}';
    }
}