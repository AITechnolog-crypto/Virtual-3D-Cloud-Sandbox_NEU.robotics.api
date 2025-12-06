package com.june.repository;

import com.june.model.TechInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TechInstanceRepository extends JpaRepository<TechInstance, Long> {
    
    List<TechInstance> findByStatus(String status);
    List<TechInstance> findByType(String type);
    List<TechInstance> findByLocation(String location);
    List<TechInstance> findByCpuUsageGreaterThan(Double threshold);
    List<TechInstance> findByMemoryUsageGreaterThan(Double threshold);
    List<TechInstance> findByTypeAndStatus(String type, String status);
    
    @Query("SELECT SUM(t.monthlyCost) FROM TechInstance t WHERE t.status = 'ONLINE'")
    Double calculateTotalMonthlyCost();
    
    @Query("SELECT t.type, SUM(t.monthlyCost), COUNT(t) FROM TechInstance t " +
           "GROUP BY t.type")
    List<Object[]> getCostBreakdownByType();
    
    @Query("SELECT t.location, SUM(t.monthlyCost), COUNT(t) FROM TechInstance t " +
           "GROUP BY t.location")
    List<Object[]> getCostBreakdownByLocation();
    
    @Query("SELECT t FROM TechInstance t WHERE t.lastHealthCheck < :threshold " +
           "OR t.lastHealthCheck IS NULL")
    List<TechInstance> findInstancesNeedingHealthCheck(LocalDateTime threshold);
    
    @Query("SELECT AVG(t.cpuUsage), AVG(t.memoryUsage), AVG(t.storageUsage) " +
           "FROM TechInstance t WHERE t.status = 'ONLINE'")
    List<Object[]> getAverageResourceUsage();
}
