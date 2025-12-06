package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class GeminiCloudAssistService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public GeminiCloudAssistService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String getInfrastructureDesign(String prompt) {
        List<String> keys = apiKeysConfig.getGoogle().getGeminiCloudAssist().getKeys();
        return execute("geminicloudassist.design", keys, prompt);
    }

    public String optimizeDeployment(String deploymentConfig) {
        List<String> keys = apiKeysConfig.getGoogle().getGeminiCloudAssist().getKeys();
        return execute("geminicloudassist.optimize", keys, deploymentConfig);
    }

    public String analyzeMonitoringData(String monitoringData) {
        List<String> keys = apiKeysConfig.getGoogle().getGeminiCloudAssist().getKeys();
        return execute("geminicloudassist.monitor", keys, monitoringData);
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String request) throws Exception {
        String url = endpoint + "?key=" + apiKey;
        logger.info("Calling Gemini Cloud Assist API: {}", url);
        // Simulate a successful response
        return "{ \"response\": \"AI-powered assistance provided for: " + request + "\" }";
    }
}
