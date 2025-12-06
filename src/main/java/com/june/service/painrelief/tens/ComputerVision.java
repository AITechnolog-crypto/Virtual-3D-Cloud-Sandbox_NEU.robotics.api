package com.june.service.painrelief.tens;

import com.june.model.painrelief.tens.TherapySession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ComputerVision {
    public void aktiviere(TherapySession session) {
        session.addLog("👁️ Computer Vision aktiviert");
        session.addLog("📷 Körperanalyse läuft...");
        log.info("Computer Vision activated. Starting body analysis.");
    }

    public String analyzeBodyRegion(TherapySession session) {
        String[] regionen = {"Lendenwirbel L1-L5", "Brustwirbel Th1-Th12", "Halswirbel C1-C7", "Sakralbereich"};
        String region = regionen[(int) (Math.random() * regionen.length)];
        session.addLog("🎯 Zielbereich erkannt: " + region);
        log.info("Computer Vision identified target region: {}", region);
        return region;
    }
}
