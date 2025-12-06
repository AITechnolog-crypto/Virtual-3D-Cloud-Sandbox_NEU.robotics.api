package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class CloudTraceService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public CloudTraceService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String listTraces(String filter) {
        List<String> keys = apiKeysConfig.getGoogle().getCloudTrace().getKeys(); // Assuming you add cloudTrace keys to ApiKeysConfig
        return execute("cloudtrace.traces.list", keys, filter);
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String request) throws Exception {
        String url = endpoint + "/v2/projects/your-project-id/traces?key=" + apiKey + "&filter=" + request;
        logger.info("Calling Google Cloud Trace API: {}", url);
        // Simulate a successful response
        return "{ \"traces\": [ { \"traceId\": \"trace-1\", \"spans\": [ { \"spanId\": \"span-1\" } ] } ] }";
    }
}
