package com.june.geospatial.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import com.june.gcloud.service.AbstractGoogleApiAgent;
import com.june.gcloud.service.GoogleKeyRotationService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class GoogleTrendsService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public GoogleTrendsService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String getInterestOverTime(String keyword) {
        List<String> keys = apiKeysConfig.getGoogle().getTrends().getKeys(); // Assuming you add trends keys to ApiKeysConfig
        return execute("trends", keys, keyword);
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String keyword) throws Exception {
        String url = endpoint + "?q=" + keyword + "&key=" + apiKey;
        logger.info("Calling Google Trends API: {}", url);
        // Simulate a successful response with a more realistic structure
        return "{\"default\":{\"timelineData\":[{\"time\":\"1609459200\",\"formattedTime\":\"Jan 2021\",\"value\":[80],\"hasData\":[true]},{\"time\":\"1612137600\",\"formattedTime\":\"Feb 2021\",\"value\":[85],\"hasData\":[true]},{\"time\":\"1614556800\",\"formattedTime\":\"Mar 2021\",\"value\":[82],\"hasData\":[true]},{\"time\":\"1617235200\",\"formattedTime\":\"Apr 2021\",\"value\":[88],\"hasData\":[true]},{\"time\":\"1619827200\",\"formattedTime\":\"May 2021\",\"value\":[90],\"hasData\":[true]}],\"averages\":[]}}";
    }
}
