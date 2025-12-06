package com.june.service.external;

import org.springframework.core.env.Environment;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Minimal SERP API client (Google engine by default).
 * Reads API key from environment SERPAPI_KEY or Spring property serp.api.key.
 * Returns raw JSON string from SerpAPI to the caller.
 */
public class SerpApiClient {
    private final HttpClient http;
    private final String apiKey;
    private final String baseUrl;

    public SerpApiClient(Environment env) {
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        String key = System.getenv("SERPAPI_KEY");
        if (key == null || key.isBlank()) {
            key = env != null ? env.getProperty("serp.api.key", "") : "";
        }
        this.apiKey = key;
        this.baseUrl = env != null ? env.getProperty("serp.api.base", "https://serpapi.com/search.json") : "https://serpapi.com/search.json";
    }

    public boolean hasKey() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String search(String q, Map<String, String> opts) throws IOException, InterruptedException {
        if (q == null || q.isBlank()) {
            throw new IllegalArgumentException("query (q) must not be empty");
        }
        Map<String, String> params = new LinkedHashMap<>();
        params.put("engine", opts != null && opts.containsKey("engine") ? opts.get("engine") : "google");
        params.put("q", q);
        if (opts != null) {
            // Allow a broader set of parameters to enable deeper/broader search.
            Set<String> allow = Set.of(
                    "location","num","hl","gl","tbm",
                    "start","page","google_domain","safe",
                    "tbs","time_period","device",
                    "as_sitesearch","include_domains","exclude_domains",
                    "filter","lr","cr","uule"
            );
            for (Map.Entry<String, String> e : opts.entrySet()) {
                String k = e.getKey();
                String v = e.getValue();
                if (allow.contains(k) && v != null && !v.isBlank()) {
                    params.put(k, v);
                }
            }
        }
        params.put("api_key", apiKey);

        StringJoiner sj = new StringJoiner("&");
        for (Map.Entry<String, String> e : params.entrySet()) {
            sj.add(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" +
                    URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8));
        }
        String url = baseUrl + "?" + sj;
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .header("User-Agent","Mozilla/5.0 (SERP Proxy)")
                .GET()
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return res.body();
    }
}
