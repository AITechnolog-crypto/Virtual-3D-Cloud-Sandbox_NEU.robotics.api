package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class CloudOptimizationService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public CloudOptimizationService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String solveRoute(String routeRequest) {
        List<String> keys = apiKeysConfig.getGoogle().getOptimization().getKeys();
        return execute("cloudoptimization.route", keys, routeRequest);
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String request) throws Exception {
        String url = endpoint + "/v1/projects/your-project-id/locations/global/operations:solve?key=" + apiKey;
        logger.info("Calling Google Cloud Optimization API: {}", url);
        // Simulate a successful response
        return "{ \"solution\": { \"routes\": [ { \"vehicleId\": \"truck-1\", \"visits\": [ { \"customer\": \"customer-A\" } ] } ] } }";
    }
}
