package com.june.gcloud.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class CloudNaturalLanguageService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;
    private final ObjectMapper objectMapper;

    public CloudNaturalLanguageService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry, ObjectMapper objectMapper) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
        this.objectMapper = objectMapper;
    }

    public String analyzeSentiment(String text) {
        List<String> keys = apiKeysConfig.getGoogle().getNaturalLanguage().getKeys();
        String requestBody = String.format("{\"document\":{\"content\":\"%s\",\"type\":\"PLAIN_TEXT\"},\"encodingType\":\"UTF8\"}", text);
        // We pass a specific service name to distinguish the action in performApiCall
        return execute("naturallanguage.sentiment", keys, requestBody);
    }

    public String analyzeEntities(String text) {
        List<String> keys = apiKeysConfig.getGoogle().getNaturalLanguage().getKeys();
        String requestBody = String.format("{\"document\":{\"content\":\"%s\",\"type\":\"PLAIN_TEXT\"},\"encodingType\":\"UTF8\"}", text);
        // We pass a specific service name to distinguish the action in performApiCall
        return execute("naturallarange.entities", keys, requestBody);
    }

    @Override
    protected String performApiCall(String apiKey, String serviceName, String requestBody) throws Exception {
        String url;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        // The 'endpoint' parameter from the abstract class is the serviceName here.
        // We use it to decide which specific API URL to build.
        if ("naturallanguage.sentiment".equals(serviceName)) {
            url = "https://language.googleapis.com/v1/documents:analyzeSentiment?key=" + apiKey;
            logger.info("Calling Google Cloud Natural Language API (Sentiment)");
            return restTemplate.postForObject(url, request, String.class);
        } else if ("naturallanguage.entities".equals(serviceName)) {
            url = "https://language.googleapis.com/v1/documents:analyzeEntities?key=" + apiKey;
            logger.info("Calling Google Cloud Natural Language API (Entities)");
            return restTemplate.postForObject(url, request, String.class);
        } else {
            logger.error("Unknown service name for CloudNaturalLanguageService: {}", serviceName);
            throw new IllegalArgumentException("Unknown service: " + serviceName);
        }
    }
}
