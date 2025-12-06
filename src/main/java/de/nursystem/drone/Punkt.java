package de.nursystem.drone;

/**
 * Repräsentiert einen Punkt im 3D-Raum
 */
class Punkt {
    private final double x;
    private final double y;
    private final double z;

    public Punkt(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }

    @Override
    public String toString() {
        return String.format("(%.4f, %.4f, %.4f)", x, y, z);
    }
}
