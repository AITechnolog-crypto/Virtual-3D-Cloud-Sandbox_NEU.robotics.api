package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class VideoIntelligenceService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public VideoIntelligenceService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String analyzeVideo(String videoUrl) {
        List<String> keys = apiKeysConfig.getGoogle().getVideoIntelligence().getKeys(); // Assuming you add videoIntelligence keys to ApiKeysConfig
        return execute("videointelligence", keys, videoUrl);
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String videoUrl) throws Exception {
        String url = endpoint + ":annotateVideo?key=" + apiKey;
        logger.info("Calling Google Video Intelligence API: {}", url);
        // In einer echten Implementierung würde hier der Request-Body für die Video Intelligence API erstellt werden.
        // Wir simulieren eine erfolgreiche Antwort.
        return "{ \"annotationResults\": [ { \"shotLabelAnnotations\": [ { \"entity\": { \"description\": \"city\" } } ] } ] }";
    }
}
