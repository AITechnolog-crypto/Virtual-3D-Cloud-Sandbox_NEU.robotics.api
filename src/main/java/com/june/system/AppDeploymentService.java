package com.june.system;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import com.june.gcloud.service.AbstractGoogleApiAgent;
import com.june.gcloud.service.GoogleKeyRotationService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class AppDeploymentService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public AppDeploymentService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String deployToCloudRun(String imageName) {
        List<String> keys = apiKeysConfig.getGoogle().getCloudRun().getKeys(); // Assuming you add cloudRun keys to ApiKeysConfig
        String requestBody = String.format("{\"template\":{\"spec\":{\"containers\":[{\"image\":\"%s\"}]}}}", imageName);
        return execute("cloudrun", keys, requestBody);
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String requestBody) throws Exception {
        String url = endpoint + "/apis/serving.knative.dev/v1/namespaces/your-namespace/services?key=" + apiKey;
        logger.info("Calling Google Cloud Run Admin API: {}", url);
        // Simulate a successful response
        return "{ \"metadata\": { \"name\": \"new-app\" }, \"status\": { \"url\": \"https://new-app-xyz.a.run.app\" } }";
    }
}
