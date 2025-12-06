package com.june.gcloud.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Ein Agent für die Interaktion mit der Google Vision API.
 * Erbt die gesamte Schlüssel-Rotations-Logik von der universellen Blaupause.
 */
@Service
public class GoogleVisionService extends AbstractGoogleApiAgent<String, Map<String, Double>> {

    private static final String VISION_SERVICE_NAME = "vision";
    private final ApiKeysConfig apiKeysConfig;

    @Autowired
    public GoogleVisionService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    /**
     * Analysiert ein Bild von einer URL und gibt die erkannten Objekte zurück.
     */
    public Map<String, Double> analyzeImage(String imageUrl) {
        List<String> keys = apiKeysConfig.getGoogle().getVision().getKeys();
        return execute(VISION_SERVICE_NAME, keys, imageUrl);
    }

    /**
     * Implementiert die spezifische Logik für den Vision API-Aufruf.
     */
    @Override
    protected Map<String, Double> performApiCall(String apiKey, String endpoint, String imageUrl) throws Exception {
        String url = endpoint + "?key=" + apiKey;
        logger.info("Calling Google Vision API: {}", url);

        // HIER WÜRDE DIE EIGENTLICHE ANFRAGE AN DIE VISION API STEHEN.
        // Wir simulieren die Analyse eines Bildes.

        if (apiKey.contains("fehler")) {
            throw new RuntimeException("Simulierter Vision API-Fehler mit diesem Schlüssel.");
        }

        // Simuliertes Ergebnis der Bildanalyse
        return Map.of(
            "Wald", 0.8,
            "Wasser", 0.15,
            "Gebäude", 0.05
        );
    }
}
