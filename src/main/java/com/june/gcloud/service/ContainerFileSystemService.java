package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ContainerFileSystemService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public ContainerFileSystemService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String getPrefetchImageReport(String image) {
        List<String> keys = apiKeysConfig.getGoogle().getContainerFileSystem().getKeys();
        return execute("containerfilesystem.getprefetchimagereport", keys, image);
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String request) throws Exception {
        String url = endpoint + "/v1/projects/your-project-id/locations/your-location/images/" + request + ":getPrefetchImageReport?key=" + apiKey;
        logger.info("Calling Container File System API: {}", url);
        // Simulate a successful response
        return "{ \"report\": { \"status\": \"SUCCESS\" } }";
    }
}
