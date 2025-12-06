package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class AdSenseService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public AdSenseService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String getAdSenseReport() {
        List<String> keys = apiKeysConfig.getGoogle().getAdsense().getKeys(); // Assuming you add adsense keys to ApiKeysConfig
        return execute("adsense", keys, "");
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String request) throws Exception {
        String url = endpoint + "?key=" + apiKey;
        logger.info("Calling Google AdSense API: {}", url);
        // Simulate a successful response
        return "{ \"reports\": [ { \"totals\": [ \"1000.00\", \"USD\" ] } ] }";
    }
}
