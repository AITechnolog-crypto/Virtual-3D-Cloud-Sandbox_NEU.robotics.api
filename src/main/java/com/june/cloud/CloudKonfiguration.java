package com.june.cloud;

/**
 * Einfache Konfigurationsklasse für Cloud-Verbindung.
 * Hält Endpoint und API-Key. Minimal gehalten, um die Beispiel-Integration zu ermöglichen.
 */
public class CloudKonfiguration {
    private String endpoint;
    private String apiKey;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
