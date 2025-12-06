package com.june.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.june.repository.DroneRepository;
import com.june.model.Drone;
import java.util.List;

@RestController
@RequestMapping("/api/drones")
@CrossOrigin(origins = "*")
public class DroneController {
    
    @Autowired
    private DroneRepository droneRepository;
    
    @GetMapping
    public List<Drone> getAllDrones() {
        return droneRepository.findAll();
    }
    
    @GetMapping("/count")
    public long countDrones() {
        return droneRepository.count();
    }
    
    @PostMapping
    public Drone createDrone(@RequestBody Drone drone) {
        return droneRepository.save(drone);
    }
}
