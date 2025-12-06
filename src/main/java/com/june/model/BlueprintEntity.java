package com.june.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "blueprints")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Blueprint {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String version;
    
    @Column(nullable = false)
    private String category; // ROBOTICS, INFRASTRUCTURE, DEFENSE, AI
    
    @Column(length = 2000)
    private String description;
    
    @Column(nullable = false)
    private String status; // DRAFT, TESTING, APPROVED, PRODUCTION, DEPRECATED
    
    private String createdBy;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
