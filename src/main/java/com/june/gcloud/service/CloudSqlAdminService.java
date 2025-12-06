package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class CloudSqlAdminService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public CloudSqlAdminService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String listInstances(String projectId) {
        List<String> keys = apiKeysConfig.getGoogle().getCloudSqlAdmin().getKeys();
        return execute(projectId, keys, "");
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String request) throws Exception {
        String url = endpoint + "/v1/projects/" + request + "/instances?key=" + apiKey;
        logger.info("Calling Cloud SQL Admin API: {}", url);
        // Simulate a successful response
        return "{ \"items\": [ { \"databaseVersion\": \"POSTGRES_14\", \"instanceType\": \"CLOUD_SQL_INSTANCE\" } ] }";
    }
}
