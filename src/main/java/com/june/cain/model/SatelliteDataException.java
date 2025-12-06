package com.june.cain.model;

public class SatelliteDataException extends Exception {
    public SatelliteDataException(String message) {
        super(message);
    }

    public SatelliteDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
