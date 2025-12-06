package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class SecretManagerService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public SecretManagerService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String accessSecret(String secretName) {
        List<String> keys = apiKeysConfig.getGoogle().getSecretManager().getKeys();
        return execute("secretmanager.access", keys, secretName);
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String request) throws Exception {
        String url = endpoint + "/v1/projects/your-project-id/secrets/" + request + ":access?key=" + apiKey;
        logger.info("Calling Secret Manager API: {}", url);
        // Simulate a successful response
        return "{ \"payload\": { \"data\": \"YOUR_SECRET_VALUE\" } }";
    }
}
