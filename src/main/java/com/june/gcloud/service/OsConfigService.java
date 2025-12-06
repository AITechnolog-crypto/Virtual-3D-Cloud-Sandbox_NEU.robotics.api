package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class OsConfigService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public OsConfigService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String listOsPolicies(String instanceId) {
        List<String> keys = apiKeysConfig.getGoogle().getOsConfig().getKeys();
        return execute("osconfig.ospolicies.list", keys, instanceId);
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String request) throws Exception {
        String url = endpoint + "/v1/projects/your-project-id/zones/your-zone/instances/" + request + "/osPolicies?key=" + apiKey;
        logger.info("Calling OS Config API: {}", url);
        // Simulate a successful response
        return "{ \"osPolicies\": [ { \"name\": \"your-os-policy\", \"state\": \"COMPLIANT\" } ] }";
    }
}
