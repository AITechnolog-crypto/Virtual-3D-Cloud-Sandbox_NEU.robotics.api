package com.june.production;

/**
 * Behandelt Fehler zentral und protokolliert eine Nachricht.
 */
public interface IErrorHandler {
    void handleException(Exception e, String message);
}