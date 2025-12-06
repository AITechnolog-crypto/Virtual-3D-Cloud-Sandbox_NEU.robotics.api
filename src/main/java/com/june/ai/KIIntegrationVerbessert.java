package com.june.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Beispiel-Entry-Point, der eine sichere, fehlertolerante KI-Integration demonstriert.
 *
 * Liest Konfiguration aus Umgebungsvariablen:
 *  - CLOUD_ENDPOINT
 *  - API_KEY
 */
public class KIIntegrationVerbessert {
    private static final Logger logger = LoggerFactory.getLogger(KIIntegrationVerbessert.class);

    public static void main(String[] args) {
        // 1. Sicheres Laden der Konfiguration aus Umgebungsvariablen
        String cloudEndpoint = System.getenv("CLOUD_ENDPOINT");
        String apiKey = System.getenv("API_KEY");

        if (cloudEndpoint == null || apiKey == null) {
            logger.error("Endpoint oder API-Schlüssel nicht gefunden. Bitte Umgebungsvariablen festlegen.");
            logger.error("Beispiel: set CLOUD_ENDPOINT=https://example.ai && set API_KEY=xyz123 (Windows Powershell/CMD)");
            return;
        }

        CloudKonfiguration konfig = new CloudKonfiguration();
        konfig.setEndpoint(cloudEndpoint);
        konfig.setApiKey(apiKey);

        KIBibliothek ki = new KIBibliothek();
        try {
            // 2. Fehlerbehandlung für Verbindung und Initialisierung
            logger.info("Versuche Verbindung zur Cloud herzustellen...");
            ki.verbindeMitCloud(konfig);
            logger.info("Verbindung erfolgreich. Initialisiere KI-Modell...");
            ki.initialisiereModell();
            logger.info("KI-Modell initialisiert.");

            // 3. Asynchrone Ausführung der Aufgabe
            String aufgabe = args != null && args.length > 0 ? String.join(" ", args) : "Ihre Aufgabe";
            logger.info("Sende Aufgabe an KI: {}", aufgabe);

            CompletableFuture.supplyAsync(() -> ki.fuehreAufgabeAus(aufgabe))
                .thenAccept(ergebnis -> logger.info("Ergebnis der KI-Aufgabe: {}", ergebnis))
                .exceptionally(e -> {
                    logger.error("Fehler bei der Ausführung der KI-Aufgabe: {}", e.getMessage());
                    return null;
                })
                // Warten bis die async Pipeline abgeschlossen wurde (kleiner Demo-Blocker)
                .join();

        } catch (KIException e) {
            logger.error("Kritischer Fehler bei der KI-Integration: {}", e.getMessage());
            // Optional: Retry-Logik oder sauberes Beenden
        }
    }
}
