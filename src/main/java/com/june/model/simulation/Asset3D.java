package com.june.model.simulation;

public class Asset3D {
    private String id;
    private String uri;
    private double x;
    private double y;
    private double z;
    private double scale;

    public Asset3D() {}

    public Asset3D(String id, String uri, double x, double y, double z, double scale) {
        this.id = id; this.uri = uri; this.x = x; this.y = y; this.z = z; this.scale = scale;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }
    public double getZ() { return z; }
    public void setZ(double z) { this.z = z; }
    public double getScale() { return scale; }
    public void setScale(double scale) { this.scale = scale; }
}