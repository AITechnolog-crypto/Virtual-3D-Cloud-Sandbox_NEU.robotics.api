package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ServiceNetworkingService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public ServiceNetworkingService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String listConnections() {
        List<String> keys = apiKeysConfig.getGoogle().getServiceNetworking().getKeys(); // Assuming you add serviceNetworking keys to ApiKeysConfig
        return execute("servicenetworking.connections.list", keys, "");
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String request) throws Exception {
        String url = endpoint + "/v1/services/servicenetworking.googleapis.com/connections?key=" + apiKey;
        logger.info("Calling Service Networking API: {}", url);
        // Simulate a successful response
        return "{ \"connections\": [ { \"network\": \"your-vpc\", \"peering\": \"servicenetworking-googleapis-com\" } ] }";
    }
}
