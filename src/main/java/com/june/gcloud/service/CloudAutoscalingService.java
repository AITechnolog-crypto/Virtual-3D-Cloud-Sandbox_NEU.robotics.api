package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class CloudAutoscalingService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public CloudAutoscalingService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String updateMetricsValues(String metricsData) {
        if (metricsData == null || metricsData.trim().isEmpty()) {
            logger.warn("CloudAutoscalingService.updateMetricsValues: 'metricsData' fehlt oder ist leer.");
            return "{ \"error\": \"metricsData required\" }";
        }
        List<String> keys = null;
        try {
            keys = (apiKeysConfig != null && apiKeysConfig.getGoogle() != null &&
                    apiKeysConfig.getGoogle().getAutoscaling() != null)
                    ? apiKeysConfig.getGoogle().getAutoscaling().getKeys()
                    : null;
        } catch (Exception e) {
            logger.warn("CloudAutoscalingService.updateMetricsValues: Fehler beim Abrufen der API-Keys: {}", e.getMessage());
        }
        if (keys == null || keys.isEmpty()) {
            logger.warn("CloudAutoscalingService.updateMetricsValues: Keine API-Keys konfiguriert. Gebe Stub-Antwort zurück.");
            return "{ \"status\": \"NO_KEYS_CONFIGURED\" }";
        }
        String response = execute("autoscaling.updatemetricsvalues", keys, metricsData);
        return response != null ? response : "{ \"status\": \"FAILED\" }";
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String request) throws Exception {
        String url = endpoint + "/v1/projects/your-project-id/locations/your-location/autoscalingPolicies:updateMetricsValues?key=" + apiKey;
        logger.info("Calling Google Cloud Autoscaling API: {}", url);
        // Simulate a successful response
        return "{ \"status\": \"SUCCESS\" }";
    }
}
