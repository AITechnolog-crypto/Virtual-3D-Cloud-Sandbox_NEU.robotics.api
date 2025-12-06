package com.june.ai;

/**
 * Domänenspezifische Ausnahme für KI-Integrationsfehler.
 */
public class KIException extends Exception {
    public KIException(String message) {
        super(message);
    }

    public KIException(String message, Throwable cause) {
        super(message, cause);
    }
}
