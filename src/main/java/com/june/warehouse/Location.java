package com.june.warehouse;

/**
 * Klasse für den Standort des Lagers.
 */
public class Location {
    private String name;
    private double latitude;
    private double longitude;

    public Location(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return name + " (" + latitude + ", " + longitude + ")";
    }
}
