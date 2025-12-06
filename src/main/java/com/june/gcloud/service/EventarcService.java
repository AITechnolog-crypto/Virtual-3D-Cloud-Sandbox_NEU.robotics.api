package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class EventarcService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public EventarcService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String createTrigger(String eventType, String targetService) {
        List<String> keys = apiKeysConfig.getGoogle().getEventarc().getKeys(); // Assuming you add eventarc keys to ApiKeysConfig
        String requestBody = String.format("{\"name\":\"projects/your-project-id/locations/your-location/triggers/new-trigger\",\"eventFilters\":[{\"attribute\":\"type\",\"value\":\"%s\"}],\"destination\":{\"cloudRun\":{\"service\":\"%s\"}}}", eventType, targetService);
        return execute("eventarc", keys, requestBody);
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String requestBody) throws Exception {
        String url = endpoint + "?key=" + apiKey;
        logger.info("Calling Google Eventarc API: {}", url);
        // Simulate a successful response
        return "{ \"name\": \"projects/your-project-id/locations/your-location/triggers/new-trigger\", \"state\": \"ACTIVE\" }";
    }
}
