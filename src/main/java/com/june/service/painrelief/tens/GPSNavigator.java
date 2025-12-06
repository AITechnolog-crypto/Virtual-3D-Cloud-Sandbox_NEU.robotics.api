package com.june.service.painrelief.tens;

import com.june.model.painrelief.tens.TherapySession;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@Slf4j
public class GPSNavigator {
    private double latitude;
    private double longitude;
    private double accuracy;

    public void aktiviere(TherapySession session) {
        this.latitude = 48.8566 + (Math.random() - 0.5) * 0.01;
        this.longitude = 2.3522 + (Math.random() - 0.5) * 0.01;
        this.accuracy = 95 + Math.random() * 5;

        session.addLog("🛰️ GPS aktiviert - Genauigkeit: " + String.format("%.1f", accuracy) + "%");
        log.info("GPS activated. Lat: {}, Lng: {}, Accuracy: {}%", latitude, longitude, accuracy);
    }

    public double[] getPosition() {
        return new double[]{latitude, longitude};
    }
}
