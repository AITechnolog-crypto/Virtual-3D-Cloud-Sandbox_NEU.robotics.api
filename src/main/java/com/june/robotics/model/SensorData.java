package com.june.robotics.model;

public class SensorData {
    private double ultrasoundDistance;
    private double infraredValue;
    private String cameraImage;

    public double getUltrasoundDistance() { return ultrasoundDistance; }
    public void setUltrasoundDistance(double distance) { this.ultrasoundDistance = distance; }
    public double getInfraredValue() { return infraredValue; }
    public void setInfraredValue(double value) { this.infraredValue = value; }
    public String getCameraImage() { return cameraImage; }
    public void setCameraImage(String image) { this.cameraImage = image; }
}
