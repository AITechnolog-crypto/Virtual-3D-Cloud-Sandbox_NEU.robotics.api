package com.june.gcloud.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class VertexAiGenerativeAiService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;
    private final ObjectMapper objectMapper;

    public VertexAiGenerativeAiService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry, ObjectMapper objectMapper) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
        this.objectMapper = objectMapper;
    }

    public String generateChat(String prompt) {
        List<String> keys = apiKeysConfig.getGoogle().getVertexAiGenerativeAi().getKeys();
        String requestBody = String.format("{\"instances\":[{\"prompt\":\"%s\"}],\"parameters\":{\"temperature\":0.2,\"maxOutputTokens\":256,\"topP\":0.95,\"topK\":40}}", prompt);
        return execute("vertexai.generativeai.chat", keys, requestBody);
    }

    public String generateCode(String prompt) {
        List<String> keys = apiKeysConfig.getGoogle().getVertexAiGenerativeAi().getKeys();
        String requestBody = String.format("{\"instances\":[{\"prompt\":\"%s\"}],\"parameters\":{\"temperature\":0.2,\"maxOutputTokens\":256,\"topP\":0.95,\"topK\":40}}", prompt);
        return execute("vertexai.generativeai.code", keys, requestBody);
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String requestBody) throws Exception {
        String url;
        if (endpoint.contains("vertexai.generativeai.chat")) {
            url = String.format("https://us-central1-aiplatform.googleapis.com/v1/projects/%s/locations/us-central1/publishers/google/models/chat-bison:predict?key=%s", apiKeysConfig.getGoogle().getCloudProject().getId(), apiKey);
            logger.info("Calling Vertex AI Generative AI (Chat): {}", url);
            return restTemplate.postForObject(url, requestBody, String.class);
        } else if (endpoint.contains("vertexai.generativeai.code")) {
            url = String.format("https://us-central1-aiplatform.googleapis.com/v1/projects/%s/locations/us-central1/publishers/google/models/code-bison:predict?key=%s", apiKeysConfig.getGoogle().getCloudProject().getId(), apiKey);
            logger.info("Calling Vertex AI Generative AI (Code): {}", url);
            return restTemplate.postForObject(url, requestBody, String.class);
        } else {
            return "";
        }
    }
}
