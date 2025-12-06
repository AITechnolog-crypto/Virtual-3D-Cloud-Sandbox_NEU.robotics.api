package com.june.drone.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Position {
    double latitude;
    double longitude;
    double altitude;

    public Position(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = 0.0; // Default altitude
    }
}
