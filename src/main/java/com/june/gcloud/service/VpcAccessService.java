package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class VpcAccessService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public VpcAccessService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String listConnectors() {
        List<String> keys = apiKeysConfig.getGoogle().getVpcAccess().getKeys(); // Assuming you add vpcAccess keys to ApiKeysConfig
        return execute("vpcaccess.connectors.list", keys, "");
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String request) throws Exception {
        String url = endpoint + "/v1/projects/your-project-id/locations/your-location/connectors?key=" + apiKey;
        logger.info("Calling Serverless VPC Access API: {}", url);
        // Simulate a successful response
        return "{ \"connectors\": [ { \"name\": \"your-connector\", \"state\": \"READY\" } ] }";
    }
}
