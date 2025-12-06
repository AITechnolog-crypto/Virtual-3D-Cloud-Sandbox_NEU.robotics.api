package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class CloudDomainsService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public CloudDomainsService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String listDomains() {
        List<String> keys = apiKeysConfig.getGoogle().getCloudDomains().getKeys(); // Assuming you add cloudDomains keys to ApiKeysConfig
        return execute("clouddomains", keys, "");
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String request) throws Exception {
        String url = endpoint + "/v1/projects/your-project-id/locations/global/registrations?key=" + apiKey;
        logger.info("Calling Google Cloud Domains API: {}", url);
        // Simulate a successful response
        return "{ \"registrations\": [ { \"name\": \"projects/your-project-id/locations/global/registrations/example.com\", \"domainName\": \"example.com\" } ] }";
    }
}
