package com.june.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Sorgt dafür, dass API-Keys aus application.properties oder Umgebungsvariablen
 * als JVM-Systemproperties zur Verfügung stehen.
 *
 * Hintergrund: Einige Hilfsklassen (z.B. ApiConnector) lesen direkt über
 * System.getProperty("openai.api.key") bzw. die ENV-Variable OPENAI_API_KEY.
 * Diese Konfiguration überführt vorhandene Spring-Properties automatisch in
 * entsprechende System-Properties, sodass beides funktioniert.
 */
@Configuration
public class ApiKeyConfigurer {

    @Bean
    CommandLineRunner propagateApiKeys(Environment env) {
        return args -> {
            // OPENAI
            setIfPresentAsSystemProperty(env, "openai.api.key", "OPENAI_API_KEY");

            // Weitere Keys optional für andere Integrationen
            setIfPresentAsSystemProperty(env, "google.gemini.api.key", "GOOGLE_GEMINI_API_KEY");
            setIfPresentAsSystemProperty(env, "azure.vision.key", "AZURE_VISION_KEY");
        };
    }

    private void setIfPresentAsSystemProperty(Environment env, String propName, String envName) {
        String value = env.getProperty(propName);
        if (isBlank(value)) {
            try {
                value = System.getenv(envName);
            } catch (Exception ignored) { }
        }
        if (!isBlank(value)) {
            System.setProperty(propName, value);
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
