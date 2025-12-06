package com.june.geospatial.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import com.june.gcloud.service.AbstractGoogleApiAgent;
import com.june.gcloud.service.GoogleKeyRotationService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class CalendarService extends AbstractGoogleApiAgent<String, String> {

    private final ApiKeysConfig apiKeysConfig;

    public CalendarService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public String createMeeting(String summary, String description, String dateTime) {
        List<String> keys = apiKeysConfig.getGoogle().getCalendar().getKeys(); // Assuming you add calendar keys to ApiKeysConfig
        String requestBody = String.format("{\"summary\":\"%s\",\"description\":\"%s\",\"start\":{\"dateTime\":\"%s\"},\"end\":{\"dateTime\":\"%s\"}}", summary, description, dateTime, dateTime);
        return execute("calendar", keys, requestBody);
    }

    @Override
    protected String performApiCall(String apiKey, String endpoint, String requestBody) throws Exception {
        String url = endpoint + "?key=" + apiKey;
        logger.info("Calling Google Calendar API: {}", url);

        // In einer echten Implementierung würde hier der Request-Body für die Calendar API erstellt werden.
        // Wir simulieren eine erfolgreiche Antwort mit einem leeren String.
        return "{\"htmlLink\":\"https://meet.google.com/lookup/random-code\"}";
    }
}
