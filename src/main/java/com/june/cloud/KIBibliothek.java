package com.june.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Minimalistische KI-„Bibliothek“ als Platzhalter/Adapter.
 * In echten Projekten würde hier ein SDK (z. B. OpenAI/Azure) angebunden.
 */
public class KIBibliothek {
    private static final Logger log = LoggerFactory.getLogger(KIBibliothek.class);

    private boolean connected;
    private boolean initialized;
    private CloudKonfiguration konfiguration;

    /**
     * Stellt (simuliert) eine Verbindung zu einer Cloud her und prüft die Konfiguration.
     */
    public void verbindeMitCloud(CloudKonfiguration konfig) throws KIException {
        Objects.requireNonNull(konfig, "Konfiguration darf nicht null sein");
        if (isBlank(konfig.getEndpoint()) || isBlank(konfig.getApiKey())) {
            throw new KIException("Ungültige Konfiguration: Endpoint oder API-Key fehlt.");
        }
        this.konfiguration = konfig;
        // Simulierter Verbindungsaufbau
        try {
            TimeUnit.MILLISECONDS.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        connected = true;
        log.debug("Mit Cloud verbunden: {}", konfig.getEndpoint());
    }

    /**
     * Initialisiert (simuliert) ein KI‑Modell nach erfolgreicher Verbindung.
     */
    public void initialisiereModell() throws KIException {
        if (!connected) {
            throw new KIException("Nicht verbunden – bitte zuerst verbindeMitCloud() aufrufen.");
        }
        try {
            TimeUnit.MILLISECONDS.sleep(120);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        initialized = true;
        log.debug("KI‑Modell initialisiert.");
    }

    /**
     * Führt (simuliert) eine Aufgabe aus und gibt ein Ergebnis zurück.
     */
    public String fuehreAufgabeAus(String aufgabe) {
        if (!initialized) {
            return "Fehler: Modell nicht initialisiert";
        }
        String safeTask = aufgabe == null ? "" : aufgabe.trim();
        try {
            TimeUnit.MILLISECONDS.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "Ergebnis(" + safeTask + ") von " + konfiguration.getEndpoint();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
