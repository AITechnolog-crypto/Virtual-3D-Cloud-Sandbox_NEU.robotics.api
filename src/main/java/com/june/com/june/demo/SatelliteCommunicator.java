package com.june.com.june.demo;

import lombok.extern.slf4j.Slf4j; /**
 * 📡 Satelliten-Kommunikator
 * Empfängt und verarbeitet Befehle über Satellitennetzwerk
 */
@Slf4j
public class SatelliteCommunicator {
    private String communicatorId;
    private boolean connected;
    private int commandsReceived;

    public SatelliteCommunicator() {
        this.communicatorId = "SAT-COMM-" + System.currentTimeMillis();
        this.connected = false;
        this.commandsReceived = 0;
        log.info("📡 Satelliten-Kommunikator {} initialisiert", communicatorId);
    }

    public SatelliteCommunicator(String communicatorId) {
        this.communicatorId = communicatorId;
        this.connected = false;
        this.commandsReceived = 0;
        log.info("📡 Satelliten-Kommunikator {} initialisiert", communicatorId);
    }

    /**
     * Verbindet mit Satellitennetzwerk
     */
    public void connect() {
        log.info("📡 Verbinde mit Satellitennetzwerk...");
        this.connected = true;
        log.info("✅ Verbindung hergestellt - Signal: STARK");
    }

    /**
     * Empfängt Befehle über Satelliten
     */
    public void receiveCommands() {
        if (!connected) {
            log.warn("⚠️ Keine Satellitenverbindung - Verbinde...");
            connect();
        }

        log.info("📡 Befehle über Satelliten empfangen");
        commandsReceived++;
        log.info("📊 Total Befehle empfangen: {}", commandsReceived);
    }

    /**
     * Sendet Status an Kontrollzentrale
     */
    public void sendStatus(String status) {
        if (connected) {
            log.info("📡 Status gesendet: {}", status);
        } else {
            log.warn("❌ Konnte Status nicht senden - Keine Verbindung");
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public int getCommandsReceived() {
        return commandsReceived;
    }

    public String getCommunicatorId() {
        return communicatorId;
    }
}
