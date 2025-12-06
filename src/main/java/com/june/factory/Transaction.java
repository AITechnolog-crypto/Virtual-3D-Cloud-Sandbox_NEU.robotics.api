package com.june.factory;

/**
 * Einfache Transaktionsbeschreibung für Blockchain-Operationen.
 */
public class Transaction {
    private final String type;
    private final String description;

    public Transaction(String type, String description) {
        this.type = type;
        this.description = description;
    }

    @Override
    public String toString() {
        return "Transaktion: Typ=" + type + ", Beschreibung=" + description;
    }
}
