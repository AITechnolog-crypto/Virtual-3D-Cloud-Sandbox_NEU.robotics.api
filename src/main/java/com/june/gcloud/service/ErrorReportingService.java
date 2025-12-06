package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ErrorReportingService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public ErrorReportingService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String listGroupStats(String projectId) {
        List<String> keys = apiKeysConfig.getGoogle().getErrorReporting().getKeys(); // Assuming you add errorReporting keys to ApiKeysConfig
        return execute(projectId, keys, "");
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String request) throws Exception {
        String url = endpoint + "/v1beta1/projects/" + request + "/groupStats?key=" + apiKey;
        logger.info("Calling Google Error Reporting API: {}", url);
        // Simulate a successful response
        return "{ \"errorGroupStats\": [ { \"count\": \"10\", \"group\": { \"groupId\": \"error-1\" } } ] }";
    }
}
