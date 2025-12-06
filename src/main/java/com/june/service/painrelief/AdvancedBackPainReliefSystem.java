package com.june.service.painrelief;

import com.june.model.painrelief.Behandlungsplan;
import com.june.model.painrelief.Schmerzdaten;
import com.june.model.painrelief.TreatmentSession;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Getter
@Setter
@Slf4j
public class AdvancedBackPainReliefSystem {
    private List<TarnDrohne> tarnDrohnen;
    private SchmerzAnalyseModul schmerzAnalyseModul;
    private BehandlungsModul behandlungsModul;
    private KIController kiController;

    public AdvancedBackPainReliefSystem(
            SchmerzAnalyseModul schmerzAnalyseModul,
            BehandlungsModul behandlungsModul,
            KIController kiController) {
        this.tarnDrohnen = new ArrayList<>();
        this.schmerzAnalyseModul = schmerzAnalyseModul;
        this.behandlungsModul = behandlungsModul;
        this.kiController = kiController;
    }

    public void aktiviereSystem(TreatmentSession session) {
        log.info("Aktiviere Advanced Back Pain Relief System für Session: {}", session.getSessionId());
        try {
            session.addLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            session.addLog("🏥 BACK PAIN RELIEF SYSTEM AKTIVIERT");
            session.addLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            // Erstelle und aktiviere Tarn-Drohne
            TarnDrohne drohne = new TarnDrohne(UUID.randomUUID().toString());
            tarnDrohnen.add(drohne);
            drohne.aktiviere(session);
            session.incrementActiveDrones();

            int cycle = 0;
            while (session.isActive() && !tarnDrohnen.isEmpty() && cycle < 50) {
                List<TarnDrohne> dronenZuEntfernen = new ArrayList<>();

                for (TarnDrohne tDrohne : tarnDrohnen) {
                    // Sammle Daten
                    Map<String, Object> daten = tDrohne.sammleDaten(session);

                    // Analysiere Schmerzen
                    Schmerzdaten schmerzdaten = schmerzAnalyseModul.analysiereSchmerzen(daten, session);
                    session.setCurrentPainLevel(schmerzdaten.getIntensitaet());

                    // Erstelle Behandlungsplan
                    Behandlungsplan plan = kiController.erstelleBehandlungsplan(schmerzdaten, session);

                    // Führe Behandlung aus
                    behandlungsModul.fuehreBehandlungAus(tDrohne, plan, session);

                    // Überprüfe, ob Schmerz gelindert wurde
                    if (schmerzAnalyseModul.istSchmerzGelindert(schmerzdaten, session)) {
                        tDrohne.deaktiviere(session);
                        dronenZuEntfernen.add(tDrohne);
                        session.decrementActiveDrones();
                    }

                    Thread.sleep(1000);
                }

                // Entferne gelinderte Drohnen
                tarnDrohnen.removeAll(dronenZuEntfernen);

                if (tarnDrohnen.isEmpty()) {
                    break;
                }

                cycle++;
            }

            session.addLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            session.addLog("✅ ALLE RÜCKENSCHMERZEN BEHANDELT!");
            session.addLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            session.setStatus("ABGESCHLOSSEN");
            log.info("Advanced Back Pain Relief System completed for Session: {}", session.getSessionId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            session.addLog("❌ Behandlung unterbrochen");
            log.warn("Advanced Back Pain Relief System interrupted for Session {}: {}", session.getSessionId(), e.getMessage());
        } catch (Exception e) {
            session.addLog("❌ Fehler: " + e.getMessage());
            log.error("Advanced Back Pain Relief System error for Session {}: {}", session.getSessionId(), e.getMessage(), e);
        }
    }
}
