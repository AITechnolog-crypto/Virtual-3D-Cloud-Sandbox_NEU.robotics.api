package com.june.gcloud.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class TranslationService extends AbstractGoogleApiAgent<Map<String, String>, String> {

    private final ApiKeysConfig apiKeysConfig;
    private final ObjectMapper objectMapper;

    public TranslationService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry, ObjectMapper objectMapper) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
        this.objectMapper = objectMapper;
    }

    public String translateText(String text, String targetLanguage) {
        List<String> keys = apiKeysConfig.getGoogle().getTranslation().getKeys(); // Assuming you add translation keys to ApiKeysConfig
        Map<String, String> request = Map.of("text", text, "targetLanguage", targetLanguage);
        return execute("translation", keys, request);
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, Map<String, String> request) throws Exception {
        String text = request.get("text");
        String targetLanguage = request.get("targetLanguage");

        String url = endpoint + "/v2?key=" + apiKey;
        logger.info("Calling Google Cloud Translation API: {}", url);

        // Simulate a successful response
        return String.format("{\"data\":{\"translations\":[{\"translatedText\":\"Translated: %s\"}]}}", text);
    }
}
