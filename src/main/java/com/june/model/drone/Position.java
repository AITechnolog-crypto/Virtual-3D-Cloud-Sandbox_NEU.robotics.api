package com.june.model.drone;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Position {
    public double latitude;
    public double longitude;

    public Position(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
