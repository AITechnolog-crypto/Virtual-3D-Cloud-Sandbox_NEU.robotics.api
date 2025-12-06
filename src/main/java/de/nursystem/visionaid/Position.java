package de.nursystem.visionaid;

/**
 * Platzhalter für die Position
 */
class Position {
    private final double latitude;
    private final double longitude;

    public Position(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    @Override
    public String toString() {
        return "(" + latitude + ", " + longitude + ")";
    }
}
