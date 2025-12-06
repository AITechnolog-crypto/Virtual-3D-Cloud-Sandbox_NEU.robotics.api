package com.june.production;

/**
 * Fehler bei der Ressourcenbeschaffung (z. B. Budget reicht nicht).
 */
public class ResourceAcquisitionException extends Exception {
    public ResourceAcquisitionException(String message) {
        super(message);
    }
}