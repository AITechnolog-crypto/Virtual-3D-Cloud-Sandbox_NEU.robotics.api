package com.june.repository;

import com.june.model.Robot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RobotRepository extends JpaRepository<Robot, Long> {
    
    Optional<Robot> findBySerialNumber(String serialNumber);
    List<Robot> findByStatus(String status);
    List<Robot> findByType(String type);
    List<Robot> findByBatteryLevelLessThan(Integer threshold);
    List<Robot> findByLastActiveAfter(LocalDateTime date);
    
    @Query("SELECT r FROM Robot r WHERE " +
           "r.positionX BETWEEN :minX AND :maxX AND " +
           "r.positionY BETWEEN :minY AND :maxY AND " +
           "r.positionZ BETWEEN :minZ AND :maxZ")
    List<Robot> findRobotsInArea(Double minX, Double maxX, 
                                   Double minY, Double maxY, 
                                   Double minZ, Double maxZ);
    
    @Query("SELECT r.status, COUNT(r) FROM Robot r GROUP BY r.status")
    List<Object[]> countByStatus();
    
    List<Robot> findByCurrentMissionContaining(String missionKeyword);
    boolean existsBySerialNumber(String serialNumber);
}
