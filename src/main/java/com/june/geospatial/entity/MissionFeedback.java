package com.june.geospatial.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Platzhalter für MissionFeedback
 */
@Data
@Document(collection = "missionFeedback")
public class MissionFeedback {
    @Id
    private String id;
    private String agentId;
    private String type;
    private String reason;
    private double lat;
    private double lng;
    private Instant timestamp;

    public MissionFeedback(String agentId, String type, String reason, double lat, double lng) {
        this.agentId = agentId;
        this.type = type;
        this.reason = reason;
        this.lat = lat;
        this.lng = lng;
        this.timestamp = Instant.now();
    }
}
