package com.june.gcloud.controller;

import com.june.gcloud.service.GeminiService;
import com.june.gcloud.service.MachineLearningOrchestrator;
import com.june.model.SecureDataContainer;
import com.june.repository.SecureDataContainerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/brain")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class BrainController {

    private final SecureDataContainerRepository secureRepo;
    private final GeminiService geminiService;
    private final MachineLearningOrchestrator mlOrchestrator;

    @PostMapping("/think")
    public ResponseEntity<Map<String, String>> think(@RequestBody String input) {
        log.info("Received thinking request with input: {}", input);
        try {
            String decision = geminiService.getCompletion(input);
            log.info("BrainController: Successfully generated decision from Gemini.");
            return ResponseEntity.ok(Map.of("decision", decision, "input", input));
        } catch (Exception e) {
            log.error("BrainController: Error getting decision from Gemini: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error getting decision from Gemini: " + e.getMessage()));
        }
    }

    @PostMapping("/train")
    public ResponseEntity<String> trainModel(@RequestParam String dataset) {
        log.info("BrainController: Request to train model with dataset: {}", dataset);
        try {
            String response = mlOrchestrator.trainModel(dataset);
            log.info("BrainController: Successfully started training model.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("BrainController: Error training model: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error training model: " + e.getMessage());
        }
    }

    @GetMapping("/status")
    public Map<String, String> getStatus() {
        log.info("Brain status requested.");
        // Status des "Hirns" zurückgeben
        return Map.of("status", "Aktiv");
    }

    @PostMapping("/secure/add")
    public Map<String, String> addSecureData(@RequestBody SecureDataContainer data) {
        log.info("Adding secure data for owner: {}", data.getOwner());
        data.setTimestamp(System.currentTimeMillis());
        secureRepo.save(data);
        return Map.of("status", "Daten sicher gespeichert");
    }

    @GetMapping("/secure/all")
    public List<SecureDataContainer> getAllSecureData() {
        log.info("Request to fetch all secure data.");
        // Zugriff kann hier weiter eingeschränkt werden
        return secureRepo.findAll();
    }
}
