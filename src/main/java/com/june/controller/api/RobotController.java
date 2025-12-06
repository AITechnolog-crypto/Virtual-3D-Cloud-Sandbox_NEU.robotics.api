package com.june.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import com.june.repository.RobotRepository;
import com.june.model.RobotEntity;
import java.util.List;

@RestController
@RequestMapping("/api/robots")
@CrossOrigin(origins = "*")
public class RobotController {

    @Autowired
    private RobotRepository robotRepository;

    @GetMapping
    public List<RobotEntity> getAllRobots() {
        return robotRepository.findAll();
    }

    @GetMapping("/{id}")
    public RobotEntity getRobotById(@PathVariable Long id) {
        return robotRepository.findById(id).orElse(null);
    }

    @PostMapping
    public RobotEntity createRobot(@RequestBody RobotEntity robot) {
        return robotRepository.save(robot);
    }

    @GetMapping("/count")
    public long countRobots() {
        return robotRepository.count();
    }
}
