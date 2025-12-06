package com.june.production.cloud;

/**
 * Exception für fehlgeschlagene Technologie-Integration.
 */
public class IntegrationFailedException extends Exception {
    public IntegrationFailedException(String message) {
        super(message);
    }

    public IntegrationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}