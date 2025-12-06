package com.june.gcloud.service;

import com.june.config.ApiRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Die universelle Blaupause für alle Google API Agenten.
 * Diese abstrakte Klasse implementiert die zentrale, robuste Logik für die API-Schlüssel-Rotation.
 * Zukünftige Agenten (Vision, Weather, etc.) erben von dieser Klasse und müssen nur noch
 * die spezifische API-Anfrage implementieren, anstatt die gesamte Logik neu zu erfinden.
 *
 * @param <P> Der Typ des Anfrage-Objekts (z.B. ein String-Prompt).
 * @param <R> Der Typ des Antwort-Objekts.
 */
public abstract class AbstractGoogleApiAgent<P, R> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final RestTemplate restTemplate;
    private final GoogleKeyRotationService keyRotationService;
    private final ApiRegistry apiRegistry;

    public AbstractGoogleApiAgent(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiRegistry apiRegistry) {
        this.keyRotationService = keyRotationService;
        this.restTemplate = restTemplate;
        this.apiRegistry = apiRegistry;
    }

    /**
     * Die öffentliche Methode, die von ausserhalb aufgerufen wird. Sie orchestriert den gesamten Prozess.
     */
    public R execute(String serviceName, List<String> keys, P request) {
        String endpoint = apiRegistry.getEndpoint(serviceName);
        if (endpoint == null) {
            logger.error("Kein Endpunkt für den Dienst '{}' in der ApiRegistry konfiguriert.", serviceName);
            return null;
        }

        if (keys == null || keys.isEmpty()) {
            logger.error("Keine API-Schlüssel für den Dienst '{}' konfiguriert.", serviceName);
            return null; // Oder eine Fehler-Antwort werfen/zurückgeben
        }

        int maxAttempts = keys.size();
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            String apiKey = keyRotationService.getNextKey(serviceName, keys);
            if (apiKey == null) {
                logger.error("KeyRotationService hat keinen Schlüssel mehr für Dienst '{}' zurückgegeben.", serviceName);
                break;
            }

            logger.info("Versuch #{} mit Dienst '{}' an Endpunkt {}", attempt + 1, serviceName, endpoint);
            try {
                // Ruft die spezifische Implementierung des Kind-Agenten auf
                R response = performApiCall(apiKey, endpoint, request);
                logger.info("✅ Erfolgreiche Antwort von Dienst '{}' bei Versuch #{}.", serviceName, attempt + 1);
                return response;

            } catch (Exception e) {
                logger.warn("Fehler bei Versuch #{} mit Dienst '{}': {}. Versuche nächsten Schlüssel.",
                        attempt + 1, serviceName, e.getMessage());
            }
        }

        logger.error("❌ Alle {} Versuche für Dienst '{}' sind fehlgeschlagen.", maxAttempts, serviceName);
        return null; // Oder eine finale Fehler-Antwort
    }

    /**
     * Die abstrakte Methode, die jeder konkrete Agent (Gemini, Vision, etc.) implementieren MUSS.
     * Hier findet die eigentliche, spezifische API-Anfrage statt.
     *
     * @param apiKey Der für diesen Versuch zu verwendende API-Schlüssel.
     * @param endpoint Der für diesen Versuch zu verwendende API-Endpunkt.
     * @param request Das an die API zu sendende Anfrage-Objekt.
     * @return Das Ergebnis des API-Aufrufs.
     * @throws Exception Wirft eine Exception bei einem Fehler, damit die `execute`-Methode zum nächsten Schlüssel wechseln kann.
     */
    protected abstract R performApiCall(String apiKey, String endpoint, P request) throws Exception;

}
