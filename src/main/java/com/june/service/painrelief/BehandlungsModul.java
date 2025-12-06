package com.june.service.painrelief;

import com.june.model.painrelief.Behandlungsplan;
import com.june.model.painrelief.TreatmentSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BehandlungsModul {

    public void fuehreBehandlungAus(TarnDrohne drohne, Behandlungsplan plan, TreatmentSession session) {
        try {
            session.addLog("💊 Behandlung wird durchgeführt...");
            session.addLog("📋 Methode: " + plan.getMethode());
            session.addLog("💉 Dosierung: " + plan.getDosierung() + " µg");
            log.info("Führe Behandlung aus mit Drohne {} nach Plan: Methode={}, Dosierung={}", drohne.getId().substring(0,8), plan.getMethode(), plan.getDosierung());

            Thread.sleep(800);

            session.addLog("✅ Behandlung erfolgreich!");
            session.incrementTreatmentsCompleted();

            // Reduziere Schmerz
            double reduction = 15 + Math.random() * 15; // 15-30%
            session.setPainReduction(session.getPainReduction() + reduction);
            log.info("Behandlung erfolgreich. Schmerzreduktion: {}%", reduction);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            session.addLog("❌ Behandlung unterbrochen");
            log.warn("Behandlung unterbrochen für Drohne {}: {}", drohne.getId().substring(0,8), e.getMessage());
        }
    }
}
