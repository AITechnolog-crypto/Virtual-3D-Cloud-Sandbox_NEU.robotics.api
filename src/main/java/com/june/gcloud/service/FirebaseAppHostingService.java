package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class FirebaseAppHostingService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public FirebaseAppHostingService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String deployToFirebase(String siteId, String version) {
        List<String> keys = apiKeysConfig.getGoogle().getFirebase().getKeys(); // Assuming you add firebase keys to ApiKeysConfig
        String requestBody = String.format("{\"config\":{\"headers\":[{\"glob\":\"**\",\"headers\":{\"Cache-Control\":\"max-age=1800\"}}]},\"version\":{\"name\":\"sites/%s/versions/%s\",\"status\":\"CREATED\"}}", siteId, version);
        return execute("firebase", keys, requestBody);
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String requestBody) throws Exception {
        String url = endpoint + "?key=" + apiKey;
        logger.info("Calling Firebase App Hosting API: {}", url);
        // Simulate a successful response
        return "{ \"name\": \"sites/your-site-id/versions/new-version\", \"status\": \"CREATED\" }";
    }
}
