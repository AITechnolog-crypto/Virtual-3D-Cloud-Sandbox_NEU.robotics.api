package com.june.controller.api;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Temporary stub API to keep the app build stable until the Drone domain is finalized.
 * Replaces repository usage with in-memory placeholders.
 */
@RestController
@RequestMapping("/api/drones")
@CrossOrigin(origins = "*")
public class DroneApiController {

    @GetMapping
    public List<Map<String, Object>> getAllDrones() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> sample = new HashMap<>();
        sample.put("id", 1);
        sample.put("name", "Demo-Drone");
        sample.put("status", "READY");
        list.add(sample);
        return list;
    }

    @GetMapping("/count")
    public long countDrones() {
        // Stubbed count until persistent repository is available
        return 1L;
    }
}
