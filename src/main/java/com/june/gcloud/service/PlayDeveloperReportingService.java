package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class PlayDeveloperReportingService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public PlayDeveloperReportingService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String getAppReports(String packageName) {
        List<String> keys = apiKeysConfig.getGoogle().getPlayDeveloperReporting().getKeys(); // Assuming you add playDeveloperReporting keys to ApiKeysConfig
        return execute(packageName, keys, "");
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String request) throws Exception {
        String url = endpoint + "/v1/applications/" + request + "/reports?key=" + apiKey;
        logger.info("Calling Google Play Developer Reporting API: {}", url);
        // Simulate a successful response
        return "{ \"reports\": [ { \"reportId\": \"report-1\", \"metrics\": { \"crashes\": 10 } } ] }";
    }
}
