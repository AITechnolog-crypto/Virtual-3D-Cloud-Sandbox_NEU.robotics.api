package com.june.service.external;

import org.springframework.core.env.Environment;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

/**
 * Minimal Azure Prediction client (generic JSON proxy)
 * - Endpoint from AZURE_PREDICTION_ENDPOINT or azure.prediction.endpoint (e.g. https://your-resource.cognitiveservices.azure.com)
 * - Region from AZURE_REGION or azure.region (default: eastus)
 * - API key from AZURE_PREDICTION_KEY or azure.prediction.key
 * - POSTs arbitrary JSON payload to a given relative path (default /predict)
 */
public class AzurePredictionClient {
    private final HttpClient http;
    private final String endpoint;
    private final String apiKey;
    private final String region;

    public AzurePredictionClient(Environment env) {
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        String ep = System.getenv("AZURE_PREDICTION_ENDPOINT");
        if (ep == null || ep.isBlank()) {
            ep = env != null ? env.getProperty("azure.prediction.endpoint", "") : "";
        }
        if (ep.endsWith("/")) ep = ep.substring(0, ep.length()-1);
        this.endpoint = ep;

        String key = System.getenv("AZURE_PREDICTION_KEY");
        if (key == null || key.isBlank()) {
            key = env != null ? env.getProperty("azure.prediction.key", "") : "";
        }
        this.apiKey = key;

        String reg = System.getenv("AZURE_REGION");
        if (reg == null || reg.isBlank()) {
            reg = env != null ? env.getProperty("azure.region", "eastus") : "eastus";
        }
        reg = normalizeRegion(reg);
        if (!isAllowedRegion(reg)) {
            reg = "eastus"; // sichere Voreinstellung (East US)
        }
        this.region = reg;
    }

    public boolean configured() {
        return endpoint != null && !endpoint.isBlank() && apiKey != null && !apiKey.isBlank() && isSafeAzureEndpoint(endpoint);
    }

    public String postJson(String relativePath, String jsonBody) throws Exception {
        if (!configured()) throw new IllegalStateException("Azure Prediction nicht konfiguriert");
        String path = (relativePath == null || relativePath.isBlank()) ? "/predict" : (relativePath.startsWith("/") ? relativePath : "/"+relativePath);
        String url = endpoint + path;
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("Ocp-Apim-Subscription-Key", apiKey)
                .header("Ocp-Apim-Subscription-Region", region)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody == null ? "{}" : jsonBody, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return res.body();
    }

    // ===== Security helpers =====
    private static boolean isSafeAzureEndpoint(String ep){
        try {
            URI u = URI.create(ep);
            if (!"https".equalsIgnoreCase(u.getScheme())) return false;
            String host = Optional.ofNullable(u.getHost()).orElse("").toLowerCase(java.util.Locale.ROOT);
            // Zulassen gängiger Azure-Domains
            return host.endsWith(".azure.com") || host.endsWith(".azure.net");
        } catch (Exception e){
            return false;
        }
    }

    private static String normalizeRegion(String r){
        if (r == null) return "eastus";
        String x = r.toLowerCase(java.util.Locale.ROOT).replaceAll("[\n\r\t ]+", "").replace("_", "").replace("-", "");
        // Häufige Tippfehler/Varianten
        if ("easus".equals(x) || "eastus".equals(x) || "eastus1".equals(x)) return "eastus";
        if ("eastus2".equals(x) || "eastusii".equals(x)) return "eastus2";
        if ("weu".equals(x) || "westeurope".equals(x)) return "westeurope";
        if ("neu".equals(x) || "northeurope".equals(x)) return "northeurope";
        if ("westus".equals(x)) return "westus";
        return x; // sonst unverändert zurückgeben
    }

    private static boolean isAllowedRegion(String r){
        java.util.Set<String> allowed = java.util.Set.of(
                "eastus","eastus2","westus","westeurope","northeurope"
        );
        return allowed.contains(r);
    }
}
