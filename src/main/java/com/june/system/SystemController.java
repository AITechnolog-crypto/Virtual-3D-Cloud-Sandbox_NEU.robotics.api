package com.june.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Der Zündungs-Mechanismus.
 * Dieser Controller bietet Endpunkte, um externe Skripte und Anwendungen vom Server aus zu starten.
 * Er umgeht damit Probleme in der lokalen Terminal-Umgebung.
 */
@RestController
@RequestMapping("/api/system")
@CrossOrigin(origins = "*")
public class SystemController {

    private static final Logger logger = LoggerFactory.getLogger(SystemController.class);

    /**
     * Der "rote Knopf", um das Python Terminal Dashboard zu starten.
     * Führt das Skript in einem neuen, eigenen Konsolenfenster aus.
     */
    @GetMapping("/launch-dashboard")
    public ResponseEntity<String> launchDashboard() {
        logger.info("🚀 Zündungs-Befehl erhalten. Starte Python Mission Control...");
        try {
            // Dieser Befehl startet das Python-Skript in einem neuen Windows-Konsolenfenster.
            // "cmd /c start" ist der Schlüssel, um den Prozess vom Java-Server zu entkoppeln.
            new ProcessBuilder("cmd", "/c", "start", "python", "terminal_dashboard.py").start();
            logger.info("  ✅ Befehl zum Starten des Dashboards erfolgreich abgesetzt.");
            return ResponseEntity.ok("Zündungs-Befehl für das Terminal Dashboard erfolgreich abgesetzt. Ein neues Konsolenfenster sollte sich öffnen.");
        } catch (IOException e) {
            logger.error("  ❌ Fehler beim Ausführen des Zündungs-Befehls: {}", e.getMessage());
            return ResponseEntity.status(500).body("Fehler beim Starten des Dashboards: " + e.getMessage());
        }
    }
}
