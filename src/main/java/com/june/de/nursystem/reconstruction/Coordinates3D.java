package com.june.de.nursystem.reconstruction;

/**
 * Repräsentiert 3D-Koordinaten
 */
public class Coordinates3D {
    private final double latitude;
    private final double longitude;
    private final double elevation;

    public Coordinates3D(double latitude, double longitude, double elevation) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getElevation() { return elevation; }

    @Override
    public String toString() {
        return "(" + latitude + ", " + longitude + ", " + elevation + ")";
    }
}
