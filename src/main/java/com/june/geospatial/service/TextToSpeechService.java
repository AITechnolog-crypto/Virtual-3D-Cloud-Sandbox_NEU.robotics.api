package com.june.geospatial.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import com.june.gcloud.service.AbstractGoogleApiAgent;
import com.june.gcloud.service.GoogleKeyRotationService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class TextToSpeechService extends AbstractGoogleApiAgent<String, byte[]> {

    private final ApiKeysConfig apiKeysConfig;

    public TextToSpeechService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public byte[] synthesizeSpeech(String text) {
        List<String> keys = apiKeysConfig.getGoogle().getTextToSpeech().getKeys(); // Assuming you add textToSpeech keys to ApiKeysConfig
        return execute("texttospeech", keys, text);
    }

    @Override
    protected byte[] performApiCall(String apiKey, String endpoint, String text) throws Exception {
        String url = endpoint + "?key=" + apiKey;
        logger.info("Calling Google Text-to-Speech API: {}", url);

        // In einer echten Implementierung würde hier der Request-Body für die Text-to-Speech API erstellt werden.
        // Wir simulieren eine erfolgreiche Antwort mit einem leeren Byte-Array.
        return new byte[0];
    }
}
