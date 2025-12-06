package com.june.controller.api;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/arm")
@CrossOrigin(origins = "*")
public class ArmController {
    @PostMapping("/move")
    public String moveArm(@RequestParam double x, @RequestParam double y, @RequestParam double z) {
        // Logik zum Bewegen des Arms
        return "{\"status\":\"Arm bewegt\",\"x\":" + x + ",\"y\":" + y + ",\"z\":" + z + "}";
    }

    @GetMapping("/status")
    public String getStatus() {
        // Status des Arms zurückgeben
        return "{\"status\":\"Bereit\"}";
    }
}

