package com.june.service;

import com.june.model.Robot;
import com.june.repository.RobotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoboticsService {
    
    @Autowired
    private RobotRepository robotRepository;
    
    /**
     * Register a new robot
     */
    @Transactional
    public Robot registerRobot(Robot robot) {
        System.out.println("Registering new robot: " + robot.getSerialNumber());
        
        if (robotRepository.existsBySerialNumber(robot.getSerialNumber())) {
            throw new RuntimeException("Robot with serial number '" + 
                                     robot.getSerialNumber() + "' already exists");
        }
        
        robot.setStatus("INACTIVE");
        robot.setCreatedAt(LocalDateTime.now());
        
        return robotRepository.save(robot);
    }
    
    /**
     * Get robot by ID
     */
    public Robot getRobot(Long id) {
        return robotRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Robot not found with id: " + id));
    }
    
    /**
     * Get all robots
     */
    public List<Robot> getAllRobots() {
        return robotRepository.findAll();
    }
    
    /**
     * Get robots by status
     */
    public List<Robot> getRobotsByStatus(String status) {
        return robotRepository.findByStatus(status);
    }
    
    /**
     * Get robots by type
     */
    public List<Robot> getRobotsByType(String type) {
        return robotRepository.findByType(type);
    }
    
    /**
     * Update robot position
     */
    @Transactional
    public Robot updateRobotPosition(Long id, Double x, Double y, Double z) {
        System.out.println("Updating position for robot " + id + ": (" + x + ", " + y + ", " + z + ")");
        
        Robot robot = getRobot(id);
        robot.setPositionX(x);
        robot.setPositionY(y);
        robot.setPositionZ(z);
        robot.setLastActive(LocalDateTime.now());
        
        return robotRepository.save(robot);
    }
    
    /**
     * Update robot battery level
     */
    @Transactional
    public Robot updateBatteryLevel(Long id, Integer batteryLevel) {
        Robot robot = getRobot(id);
        robot.setBatteryLevel(batteryLevel);
        robot.setLastActive(LocalDateTime.now());
        
        if (batteryLevel < 20) {
            System.out.println("⚠ Robot " + id + " has low battery: " + batteryLevel + "%");
        }
        
        return robotRepository.save(robot);
    }
    
    /**
     * Assign mission to robot
     */
    @Transactional
    public Robot assignMission(Long id, String mission) {
        System.out.println("Assigning mission to robot " + id + ": " + mission);
        
        Robot robot = getRobot(id);
        robot.setCurrentMission(mission);
        robot.setStatus("DEPLOYED");
        robot.setLastActive(LocalDateTime.now());
        
        return robotRepository.save(robot);
    }
    
    /**
     * Complete mission
     */
    @Transactional
    public Robot completeMission(Long id) {
        System.out.println("Completing mission for robot " + id);
        
        Robot robot = getRobot(id);
        robot.setCurrentMission(null);
        robot.setStatus("ACTIVE");
        robot.setLastActive(LocalDateTime.now());
        
        return robotRepository.save(robot);
    }
    
    /**
     * Set robot to maintenance
     */
    @Transactional
    public Robot setMaintenance(Long id) {
        Robot robot = getRobot(id);
        robot.setStatus("MAINTENANCE");
        return robotRepository.save(robot);
    }
    
    /**
     * Activate robot
     */
    @Transactional
    public Robot activateRobot(Long id) {
        Robot robot = getRobot(id);
        robot.setStatus("ACTIVE");
        robot.setLastActive(LocalDateTime.now());
        return robotRepository.save(robot);
    }
    
    /**
     * Deactivate robot
     */
    @Transactional
    public Robot deactivateRobot(Long id) {
        Robot robot = getRobot(id);
        robot.setStatus("INACTIVE");
        return robotRepository.save(robot);
    }
    
    /**
     * Find robots with low battery
     */
    public List<Robot> findRobotsWithLowBattery(Integer threshold) {
        return robotRepository.findByBatteryLevelLessThan(threshold);
    }
    
    /**
     * Find robots in specific area
     */
    public List<Robot> findRobotsInArea(Double minX, Double maxX,
                                         Double minY, Double maxY,
                                         Double minZ, Double maxZ) {
        return robotRepository.findRobotsInArea(minX, maxX, minY, maxY, minZ, maxZ);
    }
    
    /**
     * Get robotics overview
     */
    public Map<String, Object> getRoboticsOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        List<Robot> allRobots = robotRepository.findAll();
        overview.put("totalRobots", allRobots.size());
        
        List<Object[]> statusCounts = robotRepository.countByStatus();
        Map<String, Long> statusMap = new HashMap<>();
        for (Object[] row : statusCounts) {
            statusMap.put((String) row[0], (Long) row[1]);
        }
        overview.put("robotsByStatus", statusMap);
        
        List<Robot> lowBatteryRobots = robotRepository.findByBatteryLevelLessThan(20);
        overview.put("lowBatteryRobots", lowBatteryRobots);
        overview.put("lowBatteryCount", lowBatteryRobots.size());
        
        List<Robot> activeRobots = robotRepository.findByStatus("ACTIVE");
        overview.put("activeRobots", activeRobots.size());
        
        List<Robot> deployedRobots = robotRepository.findByStatus("DEPLOYED");
        overview.put("deployedRobots", deployedRobots.size());
        
        return overview;
    }
    
    /**
     * Delete robot
     */
    @Transactional
    public void deleteRobot(Long id) {
        System.out.println("Deleting robot with id: " + id);
        robotRepository.deleteById(id);
    }
}
