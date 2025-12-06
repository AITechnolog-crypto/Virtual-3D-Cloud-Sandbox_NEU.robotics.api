package com.june.service;

import com.june.model.TechInstance;
import com.june.repository.TechInstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TechInfrastructureService {
    
    @Autowired
    private TechInstanceRepository techInstanceRepository;
    
    /**
     * Create new tech instance
     */
    @Transactional
    public TechInstance createInstance(TechInstance instance) {
        System.out.println("Creating new tech instance: " + instance.getType() + " at " + instance.getLocation());
        
        instance.setStatus("OFFLINE");
        instance.setCreatedAt(LocalDateTime.now());
        instance.setLastHealthCheck(LocalDateTime.now());
        
        return techInstanceRepository.save(instance);
    }
    
    /**
     * Get instance by ID
     */
    public TechInstance getInstance(Long id) {
        return techInstanceRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Tech instance not found with id: " + id));
    }
    
    /**
     * Get all instances
     */
    public List<TechInstance> getAllInstances() {
        return techInstanceRepository.findAll();
    }
    
    /**
     * Get instances by type
     */
    public List<TechInstance> getInstancesByType(String type) {
        return techInstanceRepository.findByType(type);
    }
    
    /**
     * Get instances by status
     */
    public List<TechInstance> getInstancesByStatus(String status) {
        return techInstanceRepository.findByStatus(status);
    }
    
    /**
     * Start instance
     */
    @Transactional
    public TechInstance startInstance(Long id) {
        System.out.println("Starting tech instance: " + id);
        
        TechInstance instance = getInstance(id);
        instance.setStatus("ONLINE");
        instance.setLastHealthCheck(LocalDateTime.now());
        
        return techInstanceRepository.save(instance);
    }
    
    /**
     * Stop instance
     */
    @Transactional
    public TechInstance stopInstance(Long id) {
        System.out.println("Stopping tech instance: " + id);
        
        TechInstance instance = getInstance(id);
        instance.setStatus("OFFLINE");
        
        return techInstanceRepository.save(instance);
    }
    
    /**
     * Set instance to maintenance
     */
    @Transactional
    public TechInstance setMaintenance(Long id) {
        TechInstance instance = getInstance(id);
        instance.setStatus("MAINTENANCE");
        return techInstanceRepository.save(instance);
    }
    
    /**
     * Update resource usage
     */
    @Transactional
    public TechInstance updateResourceUsage(Long id, Double cpu, Double memory, Double storage) {
        TechInstance instance = getInstance(id);
        instance.setCpuUsage(cpu);
        instance.setMemoryUsage(memory);
        instance.setStorageUsage(storage);
        instance.setLastHealthCheck(LocalDateTime.now());
        
        if (cpu > 90.0 || memory > 90.0) {
            System.out.println("⚠ High resource usage on instance " + id + 
                             ": CPU=" + cpu + "%, Memory=" + memory + "%");
        }
        
        return techInstanceRepository.save(instance);
    }
    
    /**
     * Perform health check
     */
    @Transactional
    public TechInstance performHealthCheck(Long id) {
        TechInstance instance = getInstance(id);
        instance.setLastHealthCheck(LocalDateTime.now());
        
        System.out.println("Health check performed on instance " + id);
        
        return techInstanceRepository.save(instance);
    }
    
    /**
     * Get instances needing health check
     */
    public List<TechInstance> getInstancesNeedingHealthCheck(int hoursThreshold) {
        LocalDateTime threshold = LocalDateTime.now().minusHours(hoursThreshold);
        return techInstanceRepository.findInstancesNeedingHealthCheck(threshold);
    }
    
    /**
     * Calculate total monthly costs
     */
    public Double calculateTotalMonthlyCost() {
        Double cost = techInstanceRepository.calculateTotalMonthlyCost();
        return cost != null ? cost : 0.0;
    }
    
    /**
     * Get cost breakdown by type
     */
    public Map<String, Double> getCostBreakdownByType() {
        List<Object[]> breakdown = techInstanceRepository.getCostBreakdownByType();
        Map<String, Double> costMap = new HashMap<>();
        
        for (Object[] row : breakdown) {
            costMap.put((String) row[0], (Double) row[1]);
        }
        
        return costMap;
    }
    
    /**
     * Get cost breakdown by location
     */
    public Map<String, Double> getCostBreakdownByLocation() {
        List<Object[]> breakdown = techInstanceRepository.getCostBreakdownByLocation();
        Map<String, Double> costMap = new HashMap<>();
        
        for (Object[] row : breakdown) {
            costMap.put((String) row[0], (Double) row[1]);
        }
        
        return costMap;
    }
    
    /**
     * Get infrastructure overview
     */
    public Map<String, Object> getInfrastructureOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        List<TechInstance> allInstances = techInstanceRepository.findAll();
        overview.put("totalInstances", allInstances.size());
        
        List<TechInstance> onlineInstances = techInstanceRepository.findByStatus("ONLINE");
        overview.put("onlineCount", onlineInstances.size());
        
        List<TechInstance> offlineInstances = techInstanceRepository.findByStatus("OFFLINE");
        overview.put("offlineCount", offlineInstances.size());
        
        Double totalCost = calculateTotalMonthlyCost();
        overview.put("totalMonthlyCost", totalCost);
        
        Map<String, Double> costByType = getCostBreakdownByType();
        overview.put("costByType", costByType);
        
        List<Object[]> avgUsage = techInstanceRepository.getAverageResourceUsage();
        if (!avgUsage.isEmpty()) {
            Object[] usage = avgUsage.get(0);
            overview.put("avgCpuUsage", usage[0]);
            overview.put("avgMemoryUsage", usage[1]);
            overview.put("avgStorageUsage", usage[2]);
        }
        
        return overview;
    }
    
    /**
     * Delete instance
     */
    @Transactional
    public void deleteInstance(Long id) {
        System.out.println("Deleting tech instance with id: " + id);
        techInstanceRepository.deleteById(id);
    }
}
