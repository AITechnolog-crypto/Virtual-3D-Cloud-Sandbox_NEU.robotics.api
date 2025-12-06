package com.june.repository;

import com.june.model.Drone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DroneRepository extends JpaRepository<Drone, Long> {
    
    Optional<Drone> findByDroneId(String droneId);
    List<Drone> findByDroneType(String droneType);
    List<Drone> findByCurrentStatus(String currentStatus);
    List<Drone> findByStealthLevelGreaterThanEqual(Integer minStealthLevel);
    List<Drone> findByBatteryLevelLessThan(Integer threshold);
    List<Drone> findByAssignedMission(String mission);
    
    @Query("SELECT d FROM Drone d WHERE " +
           "d.positionX BETWEEN :minX AND :maxX AND " +
           "d.positionY BETWEEN :minY AND :maxY AND " +
           "d.positionZ BETWEEN :minZ AND :maxZ")
    List<Drone> findDronesInArea(Double minX, Double maxX,
                                   Double minY, Double maxY,
                                   Double minZ, Double maxZ);
    
    @Query("SELECT d FROM Drone d WHERE d.currentStatus = 'IDLE' " +
           "AND d.batteryLevel > :minBattery")
    List<Drone> findAvailableDrones(Integer minBattery);
    
    @Query("SELECT d.droneType, COUNT(d) FROM Drone d GROUP BY d.droneType")
    List<Object[]> countByType();
    
    @Query("SELECT d.currentStatus, COUNT(d) FROM Drone d GROUP BY d.currentStatus")
    List<Object[]> countByStatus();
    
    List<Drone> findTop10ByOrderByMaxSpeedDesc();
    List<Drone> findByLastActiveAfter(LocalDateTime date);
    boolean existsByDroneId(String droneId);
}
