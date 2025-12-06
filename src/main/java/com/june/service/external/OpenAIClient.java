package com.june.service.external;

import org.springframework.core.env.Environment;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Minimal OpenAI API Client (Chat Completions)
 * - Reads API key from env OPENAI_API_KEY or Spring property openai.api.key
 * - Default base: https://api.openai.com/v1/chat/completions
 */
public class OpenAIClient {
    private final HttpClient http;
    private final String apiKey;
    private final String baseUrl;

    public OpenAIClient(Environment env){
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        String key = System.getenv("OPENAI_API_KEY");
        if (key == null || key.isBlank()) {
            key = env != null ? env.getProperty("openai.api.key", "") : "";
        }
        this.apiKey = key;
        this.baseUrl = env != null ? env.getProperty("openai.api.base", "https://api.openai.com/v1/chat/completions") : "https://api.openai.com/v1/chat/completions";
    }

    public boolean hasKey(){
        return apiKey != null && !apiKey.isBlank();
    }

    public String chat(String model, String prompt, Integer maxTokens, Double temperature) throws Exception {
        String safeModel = (model == null || model.isBlank()) ? "gpt-4o-mini" : model.trim();
        if (!isAllowedModel(safeModel)) {
            safeModel = "gpt-4o-mini";
        }
        int mt = clamp(maxTokens == null ? 512 : maxTokens, 1, 4096);
        double temp = clamp(temperature == null ? 0.7 : temperature, 0.0, 1.0);

        String json = "{"+
                "\"model\":\""+escape(safeModel)+"\","+
                "\"messages\":[{"+
                "\"role\":\"user\",\"content\":\""+escape(prompt)+"\""+
                "}],"+
                "\"max_tokens\":"+mt+","+
                "\"temperature\":"+String.format(java.util.Locale.US, "%.3f", temp)+
                "}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer "+apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return res.body();
    }

    private static boolean isAllowedModel(String m){
        // Minimal Allowlist – extend as needed, safest small set
        return "gpt-4o-mini".equals(m) || "gpt-4o".equals(m) || "gpt-4.1-mini".equals(m) || "gpt-4.1".equals(m);
    }

    private static int clamp(int v, int min, int max){ return Math.max(min, Math.min(max, v)); }
    private static double clamp(double v, double min, double max){ return Math.max(min, Math.min(max, v)); }
    private static String escape(String s){ if (s == null) return ""; return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n"); }
}
