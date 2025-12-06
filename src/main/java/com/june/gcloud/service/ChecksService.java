package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ChecksService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public ChecksService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String listChecks() {
        List<String> keys = apiKeysConfig.getGoogle().getChecks().getKeys();
        return execute("checks.list", keys, "");
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String request) throws Exception {
        String url = endpoint + "/v1/projects/your-project-id/checks?key=" + apiKey;
        logger.info("Calling Google Checks API: {}", url);
        // Simulate a successful response
        return "{ \"checks\": [ { \"name\": \"your-check\", \"state\": \"COMPLIANT\" } ] }";
    }
}
