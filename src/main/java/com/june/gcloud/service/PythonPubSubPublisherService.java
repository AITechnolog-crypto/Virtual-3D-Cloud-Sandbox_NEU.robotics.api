package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PythonPubSubPublisherService {

    private final RestTemplate restTemplate;
    private final ApiKeysConfig apiKeysConfig;
    private final ApiRegistry apiRegistry;

    public String publishMessage(String topic, String message) {
        String pythonPublisherEndpoint = apiKeysConfig.getGoogle().getPython().getPythonPubSub().getPublisherEndpoint();
        if (pythonPublisherEndpoint == null || pythonPublisherEndpoint.isBlank()) {
            log.error("Python Pub/Sub Publisher Endpoint not configured in application.properties.");
            return "Error: Python Pub/Sub Publisher Endpoint not configured.";
        }

        String url = pythonPublisherEndpoint + "/publish";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = Map.of(
                "topic", topic,
                "message", message
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        try {
            log.info("Calling Python Pub/Sub Publisher at {}: Publishing message to topic '{}'.", url, topic);
            return restTemplate.postForObject(url, request, String.class);
        } catch (Exception e) {
            log.error("Error calling Python Pub/Sub Publisher at {}: {}", url, e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }
}
