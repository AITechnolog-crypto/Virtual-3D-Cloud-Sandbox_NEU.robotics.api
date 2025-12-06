package com.june.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.june.repository.RobotRepository;
import com.june.model.Robot;
import java.util.List;

@RestController
@RequestMapping("/api/robots")
@CrossOrigin(origins = "*")
public class RobotController {
    
    @Autowired
    private RobotRepository robotRepository;
    
    @GetMapping
    public List<Robot> getAllRobots() {
        return robotRepository.findAll();
    }
    
    @GetMapping("/{id}")
    public Robot getRobotById(@PathVariable Long id) {
        return robotRepository.findById(id).orElse(null);
    }
    
    @PostMapping
    public Robot createRobot(@RequestBody Robot robot) {
        return robotRepository.save(robot);
    }
    
    @GetMapping("/count")
    public long countRobots() {
        return robotRepository.count();
    }
}
