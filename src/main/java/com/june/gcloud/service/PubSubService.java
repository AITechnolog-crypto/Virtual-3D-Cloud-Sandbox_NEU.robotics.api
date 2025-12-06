package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class PubSubService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public PubSubService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String publishMessage(String topic, String message) {
        List<String> keys = apiKeysConfig.getGoogle().getPubsub().getKeys(); // Assuming you add pubsub keys to ApiKeysConfig
        String requestBody = String.format("{\"messages\":[{\"data\":\"%s\"}]}", message);
        return execute(topic, keys, requestBody);
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String requestBody) throws Exception {
        String url = endpoint + "/v1/projects/your-project-id/topics/" + requestBody + ":publish?key=" + apiKey;
        logger.info("Calling Google Cloud Pub/Sub API: {}", url);
        // Simulate a successful response
        return "{ \"messageIds\": [ \"1234567890\" ] }";
    }
}
