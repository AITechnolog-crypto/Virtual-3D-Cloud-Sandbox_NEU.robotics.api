package com.june.geospatial.service;

import com.june.config.ApiKeysConfig;
import com.june.config.ApiRegistry;
import com.june.gcloud.service.AbstractGoogleApiAgent;
import com.june.gcloud.service.GoogleKeyRotationService;
import com.june.geospatial.dto.PlacesNearbyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class GeospatialService extends AbstractGoogleApiAgent<String, PlacesNearbyResponse> {

    private static final String GEOSPATIAL_SERVICE_NAME = "maps"; // Using "maps" as the service name
    private final ApiKeysConfig apiKeysConfig;

    @Autowired
    public GeospatialService(GoogleKeyRotationService keyRotationService, RestTemplate restTemplate, ApiKeysConfig apiKeysConfig, ApiRegistry apiRegistry) {
        super(keyRotationService, restTemplate, apiRegistry);
        this.apiKeysConfig = apiKeysConfig;
    }

    public PlacesNearbyResponse findPlacesNearby(double lat, double lng, int radius, String type) {
        List<String> keys = apiKeysConfig.getGoogle().getMaps().getKeys();
        String requestUrl = String.format("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%f,%f&radius=%d&type=%s",
                lat, lng, radius, type);
        return execute(GEOSPATIAL_SERVICE_NAME, keys, requestUrl);
    }

    @Override
    protected PlacesNearbyResponse performApiCall(String apiKey, String endpoint, String requestUrl) throws Exception {
        String url = requestUrl + "&key=" + apiKey;
        return restTemplate.getForObject(url, PlacesNearbyResponse.class);
    }
}
