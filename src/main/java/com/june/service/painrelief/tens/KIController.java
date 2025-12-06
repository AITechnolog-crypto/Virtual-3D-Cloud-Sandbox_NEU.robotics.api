package com.june.service.painrelief.tens;

import com.june.model.painrelief.Behandlungsplan;
import com.june.model.painrelief.Schmerzdaten;
import com.june.model.painrelief.tens.TherapySession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KIController {

    public Behandlungsplan erstelleBehandlungsplan(Schmerzdaten schmerzdaten, TherapySession session) {
        session.addLog("🤖 KI analysiert Schmerzdaten...");
        log.info("KI erstellt Behandlungsplan für Schmerzintensität: {}", schmerzdaten.getIntensitaet());

        boolean tensEmpfohlen = true;
        boolean emsEmpfohlen = schmerzdaten.getIntensitaet() > 5;

        double tensFrequenz;
        double tensIntensitaet;
        double emsIntensitaet = 0;

        if (schmerzdaten.getIntensitaet() > 7) {
            tensFrequenz = 2 + Math.random() * 8; // 2-10 Hz (niedrig für chronisch)
            tensIntensitaet = 70 + Math.random() * 30; // 70-100%
            emsIntensitaet = 60 + Math.random() * 30; // 60-90%
            session.addLog("📋 KI-Plan: Intensive TENS + EMS Therapie");
            log.info("KI-Plan: Intensive TENS + EMS Therapie empfohlen.");
        } else if (schmerzdaten.getIntensitaet() > 4) {
            tensFrequenz = 50 + Math.random() * 50; // 50-100 Hz (hoch für akut)
            tensIntensitaet = 50 + Math.random() * 30; // 50-80%
            emsIntensitaet = 40 + Math.random() * 30; // 40-70%
            session.addLog("📋 KI-Plan: Moderate TENS + EMS Therapie");
            log.info("KI-Plan: Moderate TENS + EMS Therapie empfohlen.");
        } else {
            tensFrequenz = 80 + Math.random() * 70; // 80-150 Hz
            tensIntensitaet = 30 + Math.random() * 30; // 30-60%
            session.addLog("📋 KI-Plan: Leichte TENS Therapie");
            log.info("KI-Plan: Leichte TENS Therapie empfohlen.");
        }

        return new Behandlungsplan(tensEmpfohlen, tensFrequenz, tensIntensitaet,
                                  emsEmpfohlen, emsIntensitaet, schmerzdaten.getLokalisierung());
    }
}
