package com.june.geospatial.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Platzhalter für RoboticAgent
 */
@Data
@Document(collection = "roboticAgents")
public class RoboticAgent {
    @Id
    private String id;
    private String agentId;
    private double currentLat;
    private double currentLng;
    private double targetLat;
    private double targetLng;
    private String status;
    private String mission;
    private Instant lastUpdate;

    public RoboticAgent(String agentId, double currentLat, double currentLng) {
        this.agentId = agentId;
        this.currentLat = currentLat;
        this.currentLng = currentLng;
        this.status = "IDLE";
        this.mission = "NONE";
        this.lastUpdate = Instant.now();
    }
}
