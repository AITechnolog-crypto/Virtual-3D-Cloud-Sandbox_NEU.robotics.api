package com.june.service.painrelief.tens;

import com.june.model.painrelief.tens.TherapySession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TENSGeraet {
    public void startTherapy(double frequenz, double intensitaet, TherapySession session) {
        session.setTensActive(true);
        session.setTensFrequency(frequenz);
        session.setTensIntensity(intensitaet);

        session.addLog("⚡ TENS-Therapie gestartet");
        session.addLog("📊 Frequenz: " + String.format("%.1f", frequenz) + " Hz");
        session.addLog("📊 Intensität: " + String.format("%.1f", intensitaet) + "%");
        session.addLog("🎯 Wirkmechanismus: Gate-Control-Theorie");
        log.info("TENS therapy started. Freq: {} Hz, Intensity: {}%", frequenz, intensitaet);
    }
}
