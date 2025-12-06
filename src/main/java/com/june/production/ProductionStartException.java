package com.june.production;

/**
 * Fehler beim Start der Produktion (z. B. Thread unterbrochen).
 */
public class ProductionStartException extends Exception {
    public ProductionStartException(String message) {
        super(message);
    }

    public ProductionStartException(String message, Throwable cause) {
        super(message, cause);
    }
}