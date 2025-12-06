package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class AutoMLService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public AutoMLService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String trainAutoMLModel(String datasetId, String modelDisplayName) {
        List<String> keys = apiKeysConfig.getGoogle().getAutoMl().getKeys();
        String requestBody = String.format("{\"displayName\":\"%s\",\"datasetId\":\"%s\"}", modelDisplayName, datasetId);
        return execute("automl.train", keys, requestBody);
    }

    public String predictAutoMLModel(String modelId, String payload) {
        List<String> keys = apiKeysConfig.getGoogle().getAutoMl().getKeys();
        String requestBody = String.format("{\"payload\":%s}", payload);
        return execute("automl.predict", keys, requestBody);
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String requestBody) throws Exception {
        String url;
        if (endpoint.contains("automl.train")) {
            url = String.format("https://automl.googleapis.com/v1/projects/your-project-id/locations/us-central1/models?key=%s", apiKey);
            logger.info("Calling Google Cloud AutoML Training API: {}", url);
            return restTemplate.postForObject(url, requestBody, String.class);
        } else if (endpoint.contains("automl.predict")) {
            url = String.format("https://automl.googleapis.com/v1/projects/your-project-id/locations/us-central1/models/%s:predict?key=%s", requestBody, apiKey);
            logger.info("Calling Google Cloud AutoML Prediction API: {}", url);
            return restTemplate.postForObject(url, requestBody, String.class);
        } else {
            return "";
        }
    }
}
