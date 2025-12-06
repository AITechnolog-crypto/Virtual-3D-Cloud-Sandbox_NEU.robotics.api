package com.june.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "robots")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Robot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String serialNumber;
    
    @Column(nullable = false)
    private String type;
    
    @Column(nullable = false)
    private String status; // ACTIVE, INACTIVE, DEPLOYED, MAINTENANCE
    
    private Integer batteryLevel;
    
    private Double positionX;
    private Double positionY;
    private Double positionZ;
    
    private String currentMission;
    
    private LocalDateTime lastActive;
    
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastActive = LocalDateTime.now();
    }
}
