package com.june.cain.model;

import lombok.Getter;

@Getter
public class SatelliteData {
    private String region;
    private boolean valid;
    private double quality;
    private double ndvi;
    private double waterQuality;
    private int airQuality;
    private double tempAnomaly;

    public SatelliteData(String region) {
        this.region = region;
        this.valid = Math.random() > 0.1; // 90% Erfolgsrate
        this.quality = 85 + Math.random() * 15;
        this.ndvi = 0.3 + Math.random() * 0.6; // 0.3-0.9 (gesunde Vegetation)
        this.waterQuality = 7.0 + Math.random() * 2.5; // 7.0-9.5 (gut)
        this.airQuality = 5 + (int)(Math.random() * 20); // 5-25 µg/m³ (gut)
        this.tempAnomaly = -1.0 + Math.random() * 3.0; // -1.0 bis +2.0°C
    }
}
