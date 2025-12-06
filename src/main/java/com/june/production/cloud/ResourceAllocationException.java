package com.june.production.cloud;

/**
 * Exception für Fehler bei der Ressourcenzuweisung in der Cloud.
 */
public class ResourceAllocationException extends Exception {
    public ResourceAllocationException(String message) {
        super(message);
    }

    public ResourceAllocationException(String message, Throwable cause) {
        super(message, cause);
    }
}