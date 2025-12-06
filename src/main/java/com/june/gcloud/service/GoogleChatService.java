package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class GoogleChatService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public GoogleChatService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String sendMessage(String space, String message) {
        List<String> keys = apiKeysConfig.getGoogle().getChat().getKeys(); // Assuming you add chat keys to ApiKeysConfig
        String requestBody = String.format("{\"text\":\"%s\"}", message);
        return execute(space, keys, requestBody);
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String requestBody) throws Exception {
        String url = endpoint + "?key=" + apiKey;
        logger.info("Calling Google Chat API: {}", url);
        // In einer echten Implementierung würde hier der Request-Body für die Chat API erstellt werden.
        // Wir simulieren eine erfolgreiche Antwort.
        return "{ \"name\": \"spaces/your-space/messages/random-id\" }";
    }
}
