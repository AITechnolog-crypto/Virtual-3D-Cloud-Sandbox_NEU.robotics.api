package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class SpeechToTextService extends AbstractGoogleApiAgent<byte[], String> {

    private final ApiKeysConfig apiKeysConfig;

    public SpeechToTextService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String recognizeSpeech(byte[] audio) {
        List<String> keys = apiKeysConfig.getGoogle().getSpeechToText().getKeys(); // Assuming you add speechToText keys to ApiKeysConfig
        return execute("speechtotext", keys, audio);
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, byte[] audio) throws Exception {
        String url = endpoint + ":recognize?key=" + apiKey;
        logger.info("Calling Google Speech-to-Text API: {}", url);
        // In einer echten Implementierung würde hier der Request-Body für die Speech-to-Text API erstellt werden.
        // Wir simulieren eine erfolgreiche Antwort.
        return "{\"results\":[{\"alternatives\":[{\"transcript\":\"Dies ist ein transkribierter Text.\"}]}]}";
    }
}
