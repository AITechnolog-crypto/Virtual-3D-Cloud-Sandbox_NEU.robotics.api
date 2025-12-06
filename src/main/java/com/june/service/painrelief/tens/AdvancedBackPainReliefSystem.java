package com.june.service.painrelief.tens;

import com.june.model.painrelief.Behandlungsplan;
import com.june.model.painrelief.Schmerzdaten;
import com.june.model.painrelief.tens.TherapySession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Getter
@Setter
@RequiredArgsConstructor
@Slf4j
public class AdvancedBackPainReliefSystem {
    private final List<AutonomeDrohne> autonomeDrohnen = new ArrayList<>();
    private final KIController kiController;
    private final SchmerzAnalyseModul schmerzAnalyseModul; // Reusing the existing one
    private final BehandlungsModul behandlungsModul; // Reusing the existing one

    public void aktiviereSystem(TherapySession session) {
        log.info("Aktiviere Advanced TENS Pain Relief System für Session: {}", session.getSessionId());
        try {
            session.addLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            session.addLog("🏥 TENS/EMS PAIN RELIEF SYSTEM AKTIVIERT");
            session.addLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            // Erstelle und aktiviere autonome Drohne mit TENS-Gerät
            AutonomeDrohne drohne = new AutonomeDrohne(UUID.randomUUID().toString());
            autonomeDrohnen.add(drohne);
            drohne.aktiviere(session);
            session.incrementActiveDrones();

            // Hauptschleife für kontinuierliche Behandlung
            for (AutonomeDrohne tDrohne : autonomeDrohnen) {
                if (!session.isActive()) break;

                // Sammle Schmerzdaten
                Schmerzdaten schmerzdaten = tDrohne.sammleDaten(session);
                session.setCurrentPainLevel(schmerzdaten.getIntensitaet());

                // Erstelle Behandlungsplan
                Behandlungsplan plan = kiController.erstelleBehandlungsplan(schmerzdaten, session);

                // Führe Behandlung aus
                tDrohne.fuehreBehandlungAus(plan, session);

                Thread.sleep(1000);
            }

            session.addLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            session.addLog("✅ THERAPIE ERFOLGREICH ABGESCHLOSSEN!");
            session.addLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            session.setStatus("ABGESCHLOSSEN");
            log.info("Advanced TENS Pain Relief System completed for Session: {}", session.getSessionId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            session.addLog("❌ Therapie unterbrochen");
            log.warn("Advanced TENS Pain Relief System interrupted for Session {}: {}", session.getSessionId(), e.getMessage());
        } catch (Exception e) {
            session.addLog("❌ Fehler: " + e.getMessage());
            log.error("Advanced TENS Pain Relief System error for Session {}: {}", session.getSessionId(), e.getMessage(), e);
        }
    }
}
