package com.june.service.painrelief;

import com.june.model.painrelief.Behandlungsplan;
import com.june.model.painrelief.Schmerzdaten;
import com.june.model.painrelief.TreatmentSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KIController {

    public Behandlungsplan erstelleBehandlungsplan(Schmerzdaten schmerzdaten, TreatmentSession session) {
        session.addLog("🤖 KI erstellt Behandlungsplan...");
        log.info("KI erstellt Behandlungsplan für Schmerzintensität: {}", schmerzdaten.getIntensitaet());

        String methode;
        double dosierung;

        if (schmerzdaten.getIntensitaet() > 7) {
            methode = "Starke Analgesie";
            dosierung = 500 + Math.random() * 500;
        } else if (schmerzdaten.getIntensitaet() > 4) {
            methode = "Moderate Schmerzlinderung";
            dosierung = 200 + Math.random() * 300;
        } else {
            methode = "Leichte Therapie";
            dosierung = 50 + Math.random() * 150;
        }

        session.addLog("📊 KI-Empfehlung: " + methode);
        log.info("KI-Empfehlung: Methode={}, Dosierung={}", methode, dosierung);

        return new Behandlungsplan(methode, dosierung, schmerzdaten.getLokalisierung());
    }
}
