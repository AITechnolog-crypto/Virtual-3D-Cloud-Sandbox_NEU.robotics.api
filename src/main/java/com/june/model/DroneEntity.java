package com.june.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "drones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Drone {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String droneId;
    
    @Column(nullable = false)
    private String droneType; // SCOUT, DEFENSE, CARGO, STEALTH
    
    @Column(nullable = false)
    private String currentStatus; // IDLE, PATROL, MISSION, CHARGING, OFFLINE
    
    private Integer stealthLevel;
    private Integer batteryLevel;
    private Double maxSpeed;
    
    private Double positionX;
    private Double positionY;
    private Double positionZ;
    
    private String assignedMission;
    
    private LocalDateTime lastActive;
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastActive = LocalDateTime.now();
    }
}
