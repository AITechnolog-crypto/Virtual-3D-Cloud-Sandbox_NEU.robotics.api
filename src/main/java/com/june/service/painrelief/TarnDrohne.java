package com.june.service.painrelief;

import com.june.model.painrelief.TreatmentSession;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Slf4j
public class TarnDrohne {
    private String id;
    private boolean aktiv;

    public TarnDrohne(String id) {
        this.id = id;
        this.aktiv = false;
    }

    public void aktiviere(TreatmentSession session) {
        this.aktiv = true;
        session.addLog("🚁 Tarn-Drohne " + id.substring(0, 8) + " aktiviert");
        session.addLog("👁️ Stealth-Modus: Aktiv");
        log.info("TarnDrohne {} activated.", id.substring(0, 8));
    }

    public void deaktiviere(TreatmentSession session) {
        this.aktiv = false;
        session.addLog("🔴 Drohne " + id.substring(0, 8) + " deaktiviert");
        log.info("TarnDrohne {} deactivated.", id.substring(0, 8));
    }

    public Map<String, Object> sammleDaten(TreatmentSession session) {
        Map<String, Object> daten = new HashMap<>();
        daten.put("timestamp", System.currentTimeMillis());
        daten.put("droneId", id);
        daten.put("sensorData", Math.random() * 100);
        log.debug("TarnDrohne {} collecting data.", id.substring(0, 8));
        return daten;
    }
}
