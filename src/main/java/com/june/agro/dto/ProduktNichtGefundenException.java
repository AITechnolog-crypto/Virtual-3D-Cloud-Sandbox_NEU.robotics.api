package com.june.agro;

/**
 * Exception für nicht gefundene Produkte im Lager.
 */
public class ProduktNichtGefundenException extends Exception {
    public ProduktNichtGefundenException(String produktName) {
        super("Produkt '" + produktName + "' nicht im Lager gefunden.");
    }
}
