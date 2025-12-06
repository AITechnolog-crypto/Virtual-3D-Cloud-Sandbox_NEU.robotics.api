package com.june.cain.service;

import com.june.cain.model.CainSession;
import com.june.cain.model.SatelliteData;
import com.june.cain.model.SatelliteDataException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SatelliteDataProvider {

    public SatelliteDataProvider(CainSession session) {
        session.addLog("  ↳ Verbinde mit Sentinel-2 API...");
        session.addLog("  ↳ Authentifizierung erfolgreich");
        session.addLog("  ↳ Verbindung hergestellt");
        log.info("SatelliteDataProvider initialisiert");
    }

    public SatelliteData getDataForRegion(String region, CainSession session) throws SatelliteDataException {
        try {
            log.info("Rufe Satellitendaten für Region ab: {}", region);
            session.addLog("  ↳ Suche nach aktuellen Satellitenbildern...");
            Thread.sleep(1000);

            session.addLog("  ↳ Gefunden: 12 Satellitenbilder");
            session.addLog("  ↳ Auflösung: 10m pro Pixel");
            session.addLog("  ↳ Zeitraum: Letzte 7 Tage");
            session.addLog("  ↳ Wolkenbedeckung: < 10%");
            Thread.sleep(800);

            session.addLog("  ↳ Lade Satellitendaten herunter...");
            session.incrementDataDownloads();
            Thread.sleep(1200);

            session.addLog("  ↳ Download abgeschlossen: 2.4 GB");

            // Simuliere Satellitendaten
            return new SatelliteData(region);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SatelliteDataException("Download unterbrochen", e);
        } catch (Exception e) {
            throw new SatelliteDataException("Fehler beim Datenabruf", e);
        }
    }
}
