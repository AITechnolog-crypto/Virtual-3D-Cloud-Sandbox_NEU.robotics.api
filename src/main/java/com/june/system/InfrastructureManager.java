package com.june.system;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import com.june.gcloud.service.AbstractGoogleApiAgent;
import com.june.gcloud.service.GoogleKeyRotationService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class InfrastructureManager extends AbstractGoogleApiAgent<Map<String, String>, String> {

    private final ApiKeysConfig apiKeysConfig;

    public InfrastructureManager(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String listInstances(String zone) {
        List<String> keys = apiKeysConfig.getGoogle().getCompute().getKeys();
        Map<String, String> request = Map.of("zone", zone);
        return execute("compute.instances.list", keys, request);
    }

    public String startInstance(String zone, String instance) {
        List<String> keys = apiKeysConfig.getGoogle().getCompute().getKeys();
        Map<String, String> request = Map.of("zone", zone, "instance", instance);
        return execute("compute.instances.start", keys, request);
    }

    public String stopInstance(String zone, String instance) {
        List<String> keys = apiKeysConfig.getGoogle().getCompute().getKeys();
        Map<String, String> request = Map.of("zone", zone, "instance", instance);
        return execute("compute.instances.stop", keys, request);
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, Map<String, String> request) throws Exception {
        String zone = request.get("zone");
        String instance = request.get("instance");
        String projectId = apiKeysConfig.getGoogle().getCloudProject().getId();
        String url;

        if (projectId == null || projectId.isBlank()) {
            throw new IllegalStateException("Google Cloud Project ID is not configured in keys.properties (keys.google.cloud-project.id)");
        }

        if (endpoint.endsWith(".start")) {
            url = String.format("https://compute.googleapis.com/compute/v1/projects/%s/zones/%s/instances/%s/start?key=%s", projectId, zone, instance, apiKey);
            logger.info("Calling Google Compute Engine API to START instance: {}", instance);
            return restTemplate.postForObject(url, null, String.class);
        } else if (endpoint.endsWith(".stop")) {
            url = String.format("https://compute.googleapis.com/compute/v1/projects/%s/zones/%s/instances/%s/stop?key=%s", projectId, zone, instance, apiKey);
            logger.info("Calling Google Compute Engine API to STOP instance: {}", instance);
            return restTemplate.postForObject(url, null, String.class);
        } else {
            url = String.format("https://compute.googleapis.com/compute/v1/projects/%s/zones/%s/instances?key=%s", projectId, zone, apiKey);
            logger.info("Calling Google Compute Engine API to LIST instances in zone: {}", zone);
            return restTemplate.getForObject(url, String.class);
        }
    }
}
