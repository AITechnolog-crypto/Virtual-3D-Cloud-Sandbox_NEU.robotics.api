package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class KubernetesEngineService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public KubernetesEngineService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String listClusters(String projectId, String zone) {
        List<String> keys = apiKeysConfig.getGoogle().getKubernetesEngine().getKeys();
        String request = String.format("projects/%s/zones/%s/clusters", projectId, zone);
        return execute("kubernetesengine.clusters.list", keys, request);
    }

    public String getCluster(String projectId, String zone, String clusterId) {
        List<String> keys = apiKeysConfig.getGoogle().getKubernetesEngine().getKeys();
        String request = String.format("projects/%s/zones/%s/clusters/%s", projectId, zone, clusterId);
        return execute("kubernetesengine.clusters.get", keys, request);
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String request) throws Exception {
        String url;
        if (endpoint.contains("kubernetesengine.clusters.list")) {
            url = String.format("https://container.googleapis.com/v1/%s/clusters?key=%s", request, apiKey);
            logger.info("Calling Kubernetes Engine API (List Clusters): {}", url);
            return restTemplate.getForObject(url, String.class);
        } else if (endpoint.contains("kubernetesengine.clusters.get")) {
            url = String.format("https://container.googleapis.com/v1/%s?key=%s", request, apiKey);
            logger.info("Calling Kubernetes Engine API (Get Cluster): {}", url);
            return restTemplate.getForObject(url, String.class);
        } else {
            return "";
        }
    }
}
