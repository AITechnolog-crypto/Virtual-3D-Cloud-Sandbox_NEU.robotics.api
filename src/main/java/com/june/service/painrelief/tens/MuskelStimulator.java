package com.june.service.painrelief.tens;

import com.june.model.painrelief.tens.TherapySession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MuskelStimulator {
    public void stimulate(double intensitaet, TherapySession session) {
        session.setEmsActive(true);
        session.setEmsIntensity(intensitaet);

        session.addLog("💪 EMS Muskelstimulation aktiv");
        session.addLog("📊 Intensität: " + String.format("%.1f", intensitaet) + "%");
        session.addLog("🎯 Muskelgruppen: Erector Spinae, Multifidus");
        log.info("EMS stimulation started. Intensity: {}%", intensitaet);
    }
}
