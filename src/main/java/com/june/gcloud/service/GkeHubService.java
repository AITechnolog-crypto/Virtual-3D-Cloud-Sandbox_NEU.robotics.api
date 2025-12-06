package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class GkeHubService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public GkeHubService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String listMemberships(String project, String location) {
        List<String> keys = apiKeysConfig.getGoogle().getGkeHub().getKeys();
        String request = String.format("projects/%s/locations/%s", project, location);
        return execute("gkehub.memberships.list", keys, request);
    }

    public String getFeature(String project, String location, String featureId) {
        List<String> keys = apiKeysConfig.getGoogle().getGkeHub().getKeys();
        String request = String.format("projects/%s/locations/%s/features/%s", project, location, featureId);
        return execute("gkehub.features.get", keys, request);
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String request) throws Exception {
        String url;
        if (endpoint.contains("gkehub.memberships.list")) {
            url = String.format("https://gkehub.googleapis.com/v1/%s/memberships?key=%s", request, apiKey);
            logger.info("Calling GKE Hub API (List Memberships): {}", url);
            return restTemplate.getForObject(url, String.class);
        } else if (endpoint.contains("gkehub.features.get")) {
            url = String.format("https://gkehub.googleapis.com/v1/%s?key=%s", request, apiKey);
            logger.info("Calling GKE Hub API (Get Feature): {}", url);
            return restTemplate.getForObject(url, String.class);
        } else {
            return "";
        }
    }
}
