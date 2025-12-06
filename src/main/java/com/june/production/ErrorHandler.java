package com.june.production;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Zentrale Fehlerbehandlung per Logger.
 */
public class ErrorHandler implements IErrorHandler {
    private static final Logger logger = Logger.getLogger(ErrorHandler.class.getName());

    @Override
    public void handleException(Exception e, String message) {
        logger.log(Level.SEVERE, message, e);
    }
}