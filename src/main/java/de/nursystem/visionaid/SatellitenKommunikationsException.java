package de.nursystem.visionaid;

/**
 * Benutzerdefinierte Exception für Satelliten-Kommunikation
 */
class SatellitenKommunikationsException extends Exception {
    public SatellitenKommunikationsException(String message) {
        super(message);
    }
}
