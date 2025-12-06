package com.june.controller.api;

import com.june.model.SecureDataContainer;
import com.june.repository.SecureDataContainerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brain")
@CrossOrigin(origins = "*")
public class BrainController {
    @Autowired
    private SecureDataContainerRepository secureRepo;

    @PostMapping("/think")
    public String think(@RequestBody String input) {
        // Simuliere eine Entscheidung oder KI-Logik
        return "{\"decision\":\"Aktion ausgeführt\",\"input\":\"" + input + "\"}";
    }

    @GetMapping("/status")
    public String getStatus() {
        // Status des "Hirns" zurückgeben
        return "{\"status\":\"Aktiv\"}";
    }

    @PostMapping("/secure/add")
    public String addSecureData(@RequestBody SecureDataContainer data) {
        data.setTimestamp(System.currentTimeMillis());
        secureRepo.save(data);
        return "{\"status\":\"Daten sicher gespeichert\"}";
    }

    @GetMapping("/secure/all")
    public List<SecureDataContainer> getAllSecureData() {
        // Zugriff kann hier weiter eingeschränkt werden
        return secureRepo.findAll();
    }
}
