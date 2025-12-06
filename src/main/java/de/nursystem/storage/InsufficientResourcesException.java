package com.june.resources;

/**
 * Ausnahme, wenn eine Freigabe mehr Einheiten verlangt als verfügbar sind.
 */
public class InsufficientResourcesException extends Exception {
    public InsufficientResourcesException(String resourceTypeName, int requested, int available) {
        super("Nicht genügend " + resourceTypeName + " vorhanden. Angefordert: " + requested + ", Verfügbar: " + available);
    }
}
