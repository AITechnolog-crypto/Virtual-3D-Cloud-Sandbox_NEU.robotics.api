package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class PythonPubSubService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public PythonPubSubService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String publishMessage(String topic, String message) {
        List<String> keys = apiKeysConfig.getGoogle().getPythonPubSub().getKeys();
        // Hier würden wir den Endpunkt des Python-Microservice aufrufen
        String requestBody = String.format("{\"topic\":\"%s\",\"message\":\"%s\"}", topic, message);
        return execute("pythonpubsub.publish", keys, requestBody);
    }

    public String subscribeToTopic(String subscription) {
        List<String> keys = apiKeysConfig.getGoogle().getPythonPubSub().getKeys();
        // Hier würden wir den Endpunkt des Python-Microservice aufrufen
        String requestBody = String.format("{\"subscription\":\"%s\"}", subscription);
        return execute("pythonpubsub.subscribe", keys, requestBody);
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String request) throws Exception {
        // In einer echten Implementierung würde hier der HTTP-Aufruf an den Python-Microservice erfolgen.
        // Der Python-Microservice würde dann die Google Cloud Pub/Sub Bibliothek nutzen.
        logger.info("Calling Python Pub/Sub Microservice: {}", endpoint);
        // Simulieren einer erfolgreichen Antwort
        return "{ \"status\": \"SUCCESS\", \"message\": \"Python Pub/Sub operation simulated.\" }";
    }
}
