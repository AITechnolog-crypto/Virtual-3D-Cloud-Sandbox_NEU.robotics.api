package com.june.gcloud.controller;

import com.june.gcloud.service.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for interacting with the Google Gemini API.
 * This acts as an "Arm-Controller" for the GeminiService.
 */
@RestController
@RequestMapping("/api/gemini")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class GeminiController {

    private final GeminiService geminiService;

    /**
     * Sends a text prompt to the Gemini API and returns the generated response.
     * @param prompt The text prompt to send to Gemini.
     * @return The response from the Gemini API.
     */
    @GetMapping("/generate")
    public ResponseEntity<String> generateContent(@RequestParam String prompt) {
        log.info("GeminiController: Request to generate content with prompt: {}", prompt);
        try {
            String response = geminiService.getCompletion(prompt);
            log.info("GeminiController: Successfully generated content.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("GeminiController: Error generating content: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating content: " + e.getMessage());
        }
    }
}
