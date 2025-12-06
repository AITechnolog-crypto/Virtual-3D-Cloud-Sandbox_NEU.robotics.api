package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class BigQueryService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public BigQueryService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String executeQuery(String query) {
        List<String> keys = apiKeysConfig.getGoogle().getBigQuery().getKeys();
        String requestBody = String.format("{\"query\":\"%s\"}", query);
        return execute("bigquery.query", keys, requestBody);
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String requestBody) throws Exception {
        String url = endpoint + "/v2/projects/your-project-id/queries?key=" + apiKey;
        logger.info("Calling Google BigQuery API: {}", url);
        // Simulate a successful response
        return "{ \"jobComplete\": true, \"rows\": [ { \"f\": [ { \"v\": \"value1\" }, { \"v\": \"value2\" } ] } ] }";
    }
}
