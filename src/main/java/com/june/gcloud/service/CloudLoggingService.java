package com.june.gcloud.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class CloudLoggingService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;
    private final ObjectMapper objectMapper;

    public CloudLoggingService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry, ObjectMapper objectMapper) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
        this.objectMapper = objectMapper;
    }

    public String writeLogEntry(String logName, String message) {
        List<String> keys = apiKeysConfig.getGoogle().getLogging().getKeys();
        String requestBody = String.format("{\"entries\":[{\"logName\":\"projects/%s/logs/%s\",\"jsonPayload\":{\"message\":\"%s\"}}]}", apiKeysConfig.getGoogle().getCloudProject().getId(), logName, message);
        return execute("logging.write", keys, requestBody);
    }

    public String listLogEntries(String filter) {
        List<String> keys = apiKeysConfig.getGoogle().getLogging().getKeys();
        String requestBody = String.format("{\"filter\":\"%s\"}", filter);
        return execute("logging.list", keys, requestBody);
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String requestBody) throws Exception {
        String url = endpoint + "?key=" + apiKey;
        logger.info("Calling Google Cloud Logging API: {}", url);
        return restTemplate.postForObject(url, requestBody, String.class);
    }
}
