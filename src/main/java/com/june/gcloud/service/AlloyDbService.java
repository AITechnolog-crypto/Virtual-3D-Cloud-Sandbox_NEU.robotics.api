package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class AlloyDbService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public AlloyDbService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String listClusters(String projectId, String location) {
        List<String> keys = apiKeysConfig.getGoogle().getAlloyDb().getKeys();
        String request = String.format("projects/%s/locations/%s", projectId, location);
        return execute("alloydb.clusters.list", keys, request);
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String request) throws Exception {
        String url = endpoint + "/v1/" + request + "/clusters?key=" + apiKey;
        logger.info("Calling AlloyDB API: {}", url);
        // Simulate a successful response
        return "{ \"clusters\": [ { \"name\": \"your-alloydb-cluster\", \"state\": \"READY\" } ] }";
    }
}
