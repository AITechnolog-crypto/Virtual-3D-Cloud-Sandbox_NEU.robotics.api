package com.june.integrated;

/**
 * Repräsentiert Sensordaten für Training/Vorhersage.
 */
public final class SensorData {
    private final double temperature;
    private final double pressure;

    public SensorData(double temperature, double pressure) {
        this.temperature = temperature;
        this.pressure = pressure;
    }

    public double getTemperature() { return temperature; }
    public double getPressure() { return pressure; }

    @Override
    public String toString() {
        return "Temperatur: " + temperature + ", Druck: " + pressure;
    }
}
