package com.june.warehouse;

/**
 * Exception für Baufehler.
 */
public class ConstructionFailedException extends Exception {
    public ConstructionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
