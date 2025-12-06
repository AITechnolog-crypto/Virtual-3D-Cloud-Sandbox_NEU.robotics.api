package com.june.model.drone;

import com.june.service.drone.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class DrohnenSession {
    private String droneId;
    private String droneName;
    private String status;
    private boolean missionActive;
    private double battery;
    private double solarPower;
    private double speed;
    private double windSpeed;
    private double heading;
    private Map<String, Object> position;
    private List<String> logs;
    private long startTime;

    // Lazy-loaded Komponenten
    private SatellitenNavigator satellitenNavigator;
    private MagnetfeldNavigator magnetfeldNavigator;
    private FlugbahnKorrektor flugbahnKorrektor;
    private EnergieManager energieManager;
    private KISteuerung kiSteuerung;
    private Cain cain;

    // Cain Daten
    private Map<String, Object> cainData;

    public DrohnenSession(String droneId, String droneName) {
        this.droneId = droneId;
        this.droneName = droneName;
        this.status = "INITIALISIERT";
        this.missionActive = false;
        this.battery = 100.0;
        this.solarPower = 0.0;
        this.speed = 0.0;
        this.windSpeed = 0.0;
        this.heading = 0.0;
        this.position = new HashMap<>();
        this.position.put("latitude", 48.8566);
        this.position.put("longitude", 2.3522);
        this.position.put("altitude", 0.0);
        this.logs = new ArrayList<>();
        this.startTime = System.currentTimeMillis();
        this.cainData = new HashMap<>();
    }

    public void addLog(String message) {
        logs.add(message);
        if (logs.size() > 50) {
            logs.remove(0);
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("droneId", droneId);
        map.put("droneName", droneName);
        map.put("status", status);
        map.put("missionActive", missionActive);
        map.put("battery", battery);
        map.put("solarPower", solarPower);
        map.put("speed", speed);
        map.put("windSpeed", windSpeed);
        map.put("heading", heading);
        map.put("position", position);
        map.put("logs", logs);
        map.put("uptime", System.currentTimeMillis() - startTime);
        return map;
    }
}
