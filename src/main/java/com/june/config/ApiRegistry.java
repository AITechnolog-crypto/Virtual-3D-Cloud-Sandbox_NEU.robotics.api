package com.june.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "api")
public class ApiRegistry {

    private final Map<String, String> endpoints = new HashMap<>();

    public Map<String, String> getEndpoints() {
        return endpoints;
    }

    // Setter nötig, damit Spring Boot die Map aus properties binden kann
    public void setEndpoints(Map<String, String> endpoints) {
        this.endpoints.clear();
        if (endpoints != null) {
            this.endpoints.putAll(endpoints);
        }
    }

    public String getEndpoint(String serviceName) {
        return endpoints.get(serviceName);
    }
}
