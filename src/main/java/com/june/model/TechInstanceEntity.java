package com.june.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "tech_instances")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechInstance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String type; // SERVER, DATABASE, STORAGE, NETWORK, AI_COMPUTE
    
    @Column(nullable = false)
    private String status; // ONLINE, OFFLINE, MAINTENANCE
    
    private String location;
    
    private Double cpuUsage;
    private Double memoryUsage;
    private Double storageUsage;
    
    private Double monthlyCost;
    
    private LocalDateTime lastHealthCheck;
    
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastHealthCheck = LocalDateTime.now();
    }
}
