package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ServiceDirectoryService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public ServiceDirectoryService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String listServices(String namespace) {
        List<String> keys = apiKeysConfig.getGoogle().getServiceDirectory().getKeys(); // Assuming you add serviceDirectory keys to ApiKeysConfig
        return execute(namespace, keys, "");
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String request) throws Exception {
        String url = endpoint + "/v1/projects/your-project-id/locations/your-location/namespaces/" + request + "/services?key=" + apiKey;
        logger.info("Calling Google Service Directory API: {}", url);
        // Simulate a successful response
        return "{ \"services\": [ { \"name\": \"projects/your-project-id/locations/your-location/namespaces/your-namespace/services/your-service\" } ] }";
    }
}
