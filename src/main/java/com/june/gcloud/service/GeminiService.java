package com.june.gcloud.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Ein Agent für die Interaktion mit der Google Gemini API.
 * Erbt die gesamte Schlüssel-Rotations-Logik von der universellen Blaupause.
 */
@Service
public class GeminiService extends AbstractGoogleApiAgent<String, String> {

    private static final String GEMINI_SERVICE_NAME = "gemini";
    private final ApiKeysConfig apiKeysConfig;
    private final ObjectMapper objectMapper;

    @Autowired
    public GeminiService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry, ObjectMapper objectMapper) {
        // Ruft den Konstruktor der Blaupause auf und übergibt die benötigten Werkzeuge
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
        this.objectMapper = objectMapper;
    }

    /**
     * Die öffentliche Methode, die die Anfrage an die übergeordnete Logik der Blaupause weiterleitet.
     */
    public String getCompletion(String prompt) {
        List<String> keys = apiKeysConfig.getGemini().getKeys();
        // Ruft die zentrale `execute`-Methode der Blaupause auf
        String response = execute(GEMINI_SERVICE_NAME, keys, prompt);
        return response != null ? response : "Fehler: Konnte keine Verbindung zur Gemini API herstellen.";
    }

    /**
     * Dies ist die einzige Methode, die wir implementieren müssen.
     * Sie enthält die spezifische Logik nur für den Gemini API-Aufruf.
     */
    @Override
    protected String performApiCall(String apiKey, String endpoint, String prompt) throws Exception {
        String url = endpoint + "?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

        String response = restTemplate.postForObject(url, request, String.class);

        // Parse the response to extract the text
        Map<String, Object> responseMap = objectMapper.readValue(response, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");
        if (candidates != null && !candidates.isEmpty()) {
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            if (content != null) {
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    return (String) parts.get(0).get("text");
                }
            }
        }

        return "Keine Antwort von Gemini erhalten.";
    }
}
