package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class GeminiForGcpService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public GeminiForGcpService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String getCodeSuggestions(String codeSnippet) {
        List<String> keys = apiKeysConfig.getGoogle().getGeminiForGcp().getKeys();
        return execute("geminiforgcp.codesuggestions", keys, codeSnippet);
    }

    public String optimizeDeployment(String deploymentConfig) {
        List<String> keys = apiKeysConfig.getGoogle().getGeminiForGcp().getKeys();
        return execute("geminiforgcp.deploymentoptimization", keys, deploymentConfig);
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String request) throws Exception {
        String url = endpoint + "?key=" + apiKey;
        logger.info("Calling Gemini for Google Cloud API: {}", url);
        // Simulate a successful response
        return "{ \"suggestions\": [ \"AI-powered code suggestion for: " + request + "\" ] }";
    }
}
