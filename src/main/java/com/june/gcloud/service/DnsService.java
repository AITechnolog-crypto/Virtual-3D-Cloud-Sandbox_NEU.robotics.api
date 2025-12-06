package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class DnsService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public DnsService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String listDnsRecords(String zone) {
        List<String> keys = apiKeysConfig.getGoogle().getDns().getKeys(); // Assuming you add dns keys to ApiKeysConfig
        return execute(zone, keys, "");
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String request) throws Exception {
        String url = endpoint + "/dns/v1/projects/your-project-id/managedZones/" + request + "/rrsets?key=" + apiKey;
        logger.info("Calling Google Cloud DNS API: {}", url);
        // Simulate a successful response
        return "{ \"rrsets\": [ { \"name\": \"example.com.\", \"type\": \"A\", \"rrdatas\": [ \"1.2.3.4\" ] } ] }";
    }
}
