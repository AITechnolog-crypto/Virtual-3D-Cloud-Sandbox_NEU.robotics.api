package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class CloudMonitoringService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public CloudMonitoringService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String getMetricData(String metricType, String filter) {
        if (metricType == null || metricType.trim().isEmpty()) {
            logger.warn("CloudMonitoringService.getMetricData: 'metricType' fehlt oder ist leer.");
            return "{ \"error\": \"metricType required\" }";
        }
        if (filter == null) {
            filter = "";
        }
        List<String> keys = null;
        try {
            keys = (apiKeysConfig != null && apiKeysConfig.getGoogle() != null &&
                    apiKeysConfig.getGoogle().getMonitoring() != null)
                    ? apiKeysConfig.getGoogle().getMonitoring().getKeys()
                    : null;
        } catch (Exception e) {
            logger.warn("CloudMonitoringService.getMetricData: Fehler beim Abrufen der API-Keys: {}", e.getMessage());
        }
        if (keys == null || keys.isEmpty()) {
            logger.warn("CloudMonitoringService.getMetricData: Keine API-Keys konfiguriert. Gebe Stub-Antwort zurück.");
            return "{ \"timeSeries\": [] }";
        }
        String requestBody = String.format("{\"filter\":\"%s\",\"interval\":{\"endTime\":\"2024-01-01T00:00:00Z\",\"startTime\":\"2023-01-01T00:00:00Z\"},\"metricName\":\"%s\"}", filter, metricType);
        String response = execute("monitoring.metrics.read", keys, requestBody);
        return response != null ? response : "{ \"timeSeries\": [] }";
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String requestBody) throws Exception {
        String url = endpoint + "/v3/projects/your-project-id/timeSeries:query?key=" + apiKey;
        logger.info("Calling Google Cloud Monitoring API: {}", url);
        // Simulate a successful response
        return "{ \"timeSeries\": [ { \"metric\": { \"type\": \"compute.googleapis.com/instance/cpu/utilization\" }, \"points\": [ { \"interval\": { \"endTime\": \"2024-01-01T00:00:00Z\" }, \"value\": { \"doubleValue\": 0.5 } } ] } ] }";
    }
}
