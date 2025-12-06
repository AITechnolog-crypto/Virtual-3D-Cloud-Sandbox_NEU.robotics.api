package com.june.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Das Kommunikationszentrum für den Architekt-Agenten.
 * Stellt die Erkenntnisse des Agenten über API-Endpunkte als Dashboard bereit.
 */
@RestController
@RequestMapping("/api/architect")
@CrossOrigin(origins = "*")
public class ArchitectController {

    private final ApplicationContext applicationContext;
    private final RestTemplate restTemplate;

    @Value("${server.port}")
    private int serverPort;

    @Autowired
    public ArchitectController(ApplicationContext applicationContext, RestTemplate restTemplate) {
        this.applicationContext = applicationContext;
        this.restTemplate = restTemplate;
    }

    /**
     * GIBT DIE ARCHITEKTUR-KARTE ZURÜCK
     * Liefert eine Liste aller aktiven Komponenten (Zahnräder) im System.
     */
    @GetMapping("/blueprints")
    public ResponseEntity<Map<String, List<String>>> getArchitectureBlueprints() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        List<String> juneBeans = Arrays.stream(beanNames)
                .filter(name -> name.startsWith("com.june"))
                .collect(Collectors.toList());

        Map<String, List<String>> response = Map.of("activeComponents", juneBeans);
        return ResponseEntity.ok(response);
    }

    /**
     * GIBT DEN LIVE-STATUSBERICHT ZURÜCK
     * Ruft die Status-Endpunkte anderer Dienste ab und fasst sie zusammen.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        Map<String, Object> statusReport = new java.util.LinkedHashMap<>();
        String baseUrl = "http://localhost:" + serverPort;
        try {
            Object gcloudStatus = restTemplate.getForObject(baseUrl + "/api/gcloud/status", Object.class);
            statusReport.put("googleCloudStatus", gcloudStatus);
        } catch (Exception e) {
            statusReport.put("googleCloudStatus", "ERROR: " + e.getMessage());
        }
        try {
            Object geoStats = restTemplate.getForObject(baseUrl + "/api/geospatial/stats", Object.class);
            statusReport.put("geospatialStats", geoStats);
        } catch (Exception e) {
            statusReport.put("geospatialStats", "ERROR: " + e.getMessage());
        }
        return ResponseEntity.ok(statusReport);
    }

    /**
     * GIBT DAS GEOSPATIAL-LOGBUCH ZURÜCK
     * Zeigt die 10 neuesten Einträge aus dem Gedächtnis an.
     */
    @GetMapping("/geospatial-log")
    public ResponseEntity<Object> getGeospatialLog() {
        String baseUrl = "http://localhost:" + serverPort;
        try {
            Object logEntries = restTemplate.getForObject(baseUrl + "/api/geospatial/log", Object.class);
            return ResponseEntity.ok(logEntries);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("ERROR: Could not retrieve geospatial log: " + e.getMessage());
        }
    }
}
