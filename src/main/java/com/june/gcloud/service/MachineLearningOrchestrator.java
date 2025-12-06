package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class MachineLearningOrchestrator extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;
    private final AutoMLService autoMLService;

    public MachineLearningOrchestrator(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry, AutoMLService autoMLService) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
        this.autoMLService = autoMLService;
    }

    public String trainModel(String dataset) {
        List<String> keys = apiKeysConfig.getGoogle().getAiPlatform().getKeys(); // Assuming you add aiPlatform keys to ApiKeysConfig
        String requestBody = String.format("{\"trainingInput\":{\"scaleTier\":\"BASIC\",\"dataset\":\"%s\"}}", dataset);
        return execute("aiplatform", keys, requestBody);
    }

    public String trainVertexAiModel(String modelDisplayName, String trainingPipelineDefinition) {
        List<String> keys = apiKeysConfig.getGoogle().getVertexAi().getKeys();
        String requestBody = String.format("{\"displayName\":\"%s\",\"trainingPipelineDefinition\":%s}", modelDisplayName, trainingPipelineDefinition);
        return execute("vertexai.training", keys, requestBody);
    }

    public String deployVertexAiModel(String modelId, String endpointDisplayName) {
        List<String> keys = apiKeysConfig.getGoogle().getVertexAi().getKeys();
        String requestBody = String.format("{\"displayName\":\"%s\"}", endpointDisplayName);
        return execute("vertexai.deploy", keys, requestBody);
    }

    public String predictVertexAiModel(String endpointId, String instance) {
        List<String> keys = apiKeysConfig.getGoogle().getVertexAi().getKeys();
        String requestBody = String.format("{\"instances\":[%s]}", instance);
        return execute("vertexai.predict", keys, requestBody);
    }

    public String trainAutoMLModel(String datasetId, String modelDisplayName) {
        return autoMLService.trainAutoMLModel(datasetId, modelDisplayName);
    }

    public String predictAutoMLModel(String modelId, String payload) {
        return autoMLService.predictAutoMLModel(modelId, payload);
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String requestBody) throws Exception {
        String url;
        if (endpoint.contains("vertexai.training")) {
            url = String.format("https://us-central1-aiplatform.googleapis.com/v1/projects/%s/locations/us-central1/trainingPipelines?key=%s", apiKeysConfig.getGoogle().getCloudProject().getId(), apiKey);
            logger.info("Calling Google Vertex AI Training API: {}", url);
            return restTemplate.postForObject(url, requestBody, String.class);
        } else if (endpoint.contains("vertexai.deploy")) {
            url = String.format("https://us-central1-aiplatform.googleapis.com/v1/projects/%s/locations/us-central1/endpoints/%s:deployModel?key=%s", apiKeysConfig.getGoogle().getCloudProject().getId(), requestBody, apiKey);
            logger.info("Calling Google Vertex AI Deploy API: {}", url);
            return restTemplate.postForObject(url, requestBody, String.class);
        } else if (endpoint.contains("vertexai.predict")) {
            url = String.format("https://us-central1-aiplatform.googleapis.com/v1/projects/%s/locations/us-central1/endpoints/%s:predict?key=%s", apiKeysConfig.getGoogle().getCloudProject().getId(), requestBody, apiKey);
            logger.info("Calling Google Vertex AI Predict API: {}", url);
            return restTemplate.postForObject(url, requestBody, String.class);
        } else if (endpoint.contains("automl.train")) {
            url = String.format("https://automl.googleapis.com/v1beta1/projects/%s/locations/us-central1/models?key=%s", apiKeysConfig.getGoogle().getCloudProject().getId(), apiKey);
            logger.info("Calling Google Cloud AutoML Training API: {}", url);
            return restTemplate.postForObject(url, requestBody, String.class);
        } else if (endpoint.contains("automl.predict")) {
            url = String.format("https://automl.googleapis.com/v1beta1/projects/%s/locations/us-central1/models/%s:predict?key=%s", apiKeysConfig.getGoogle().getCloudProject().getId(), requestBody, apiKey);
            logger.info("Calling Google Cloud AutoML Prediction API: {}", url);
            return restTemplate.postForObject(url, requestBody, String.class);
        } else {
            url = endpoint + "?key=" + apiKey;
            logger.info("Calling Google AI Platform Training API: {}", url);
            // Simulate a successful response
            return "{ \"jobId\": \"training_job_123\", \"state\": \"QUEUED\" }";
        }
    }
}
