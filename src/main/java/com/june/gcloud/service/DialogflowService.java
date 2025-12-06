package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class DialogflowService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public DialogflowService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String createAgent(String agentName) {
        List<String> keys = apiKeysConfig.getGoogle().getDialogflow().getKeys(); // Assuming you add dialogflow keys to ApiKeysConfig
        String requestBody = String.format("{\"displayName\":\"%s\",\"defaultLanguageCode\":\"en\",\"timeZone\":\"America/Los_Angeles\"}", agentName);
        return execute("dialogflow.agents.create", keys, requestBody);
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String requestBody) throws Exception {
        String url = endpoint + "?key=" + apiKey;
        logger.info("Calling Google Dialogflow API: {}", url);
        // Simulate a successful response
        return String.format("{\"name\":\"projects/your-project-id/agent\",\"displayName\":\"%s\",\"defaultLanguageCode\":\"en\"}", requestBody);
    }
}
