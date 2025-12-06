package com.june.spineaid;

import com.june.spineaid.model.Behandlungsplan;
import com.june.spineaid.model.Patient;
import com.june.spineaid.model.SpineAidSession;
import com.june.spineaid.service.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Provider; // Import for Provider
import java.util.ArrayList;
import java.util.List;

@Service
@Getter
@Setter
@RequiredArgsConstructor
@Slf4j
public class SpineAidNetSystem {
    // 7 Basis-Komponenten (injected as singletons)
    private final VRTherapieModul vrTherapieModul;
    private final MuskelStimulator muskelStimulator;
    private final TENSGeraet tensGeraet;
    private final SensorModul sensorModul;
    private final Benutzeroberflaeche ui;
    private final SicherheitsManager sicherheitsManager;
    private final DatenManager datenManager;

    // Advanced Components (injected as singletons)
    private final KIController kiController;
    private final BlockchainWallet blockchainWallet;
    private final CloudDatenManager cloudDatenManager;

    // Providers for dynamically created instances (if these are meant to be prototypes)
    private final Provider<VRTherapieModul> vrTherapieModulProvider;
    private final Provider<MuskelStimulator> muskelStimulatorProvider;
    private final Provider<TENSGeraet> tensGeraetProvider;

    // ArrayLists for extended functionality (initialized empty, populated via providers)
    private final List<VRTherapieModul> vrTherapieModule = new ArrayList<>();
    private final List<MuskelStimulator> muskelStimulatoren = new ArrayList<>();
    private final List<TENSGeraet> tensGeraete = new ArrayList<>();

    public void aktiviereSystem(SpineAidSession session) {
        log.info("SpineAidNetSystem: Aktiviere System für Patient {}.", session.getPatientName());
        try {
            session.addLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            session.addLog("🏥 SPINEAIDNET SYSTEM AKTIVIERUNG");
            session.addLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            // SCHRITT 1: Sicherheitsüberprüfung
            session.addLog("🔐 Schritt 1: Sicherheitsüberprüfung");
            if (sicherheitsManager.ueberpruefeSicherheit(session)) {
                session.setSecurityPassed(true);
                session.addLog("✅ Sicherheitsüberprüfung erfolgreich");
                log.info("SpineAidNetSystem: Sicherheitsüberprüfung erfolgreich für Patient {}.", session.getPatientName());
            } else {
                session.addLog("❌ Sicherheitsüberprüfung fehlgeschlagen");
                ui.zeigeFehlermeldung("Sicherheitsüberprüfung fehlgeschlagen.", session);
                session.setStatus("FEHLER");
                log.error("SpineAidNetSystem: Sicherheitsüberprüfung fehlgeschlagen für Patient {}.", session.getPatientName());
                return;
            }
            Thread.sleep(1000);

            // SCHRITT 2: Blockchain-Finanzierung
            session.addLog("💰 Schritt 2: Blockchain-Finanzierung");
            double amountNeeded = kiController.calculateFundsNeeded(session);
            blockchainWallet.transferFunds(amountNeeded, session);
            session.incrementFundsTransferred(amountNeeded);
            log.info("SpineAidNetSystem: Blockchain-Finanzierung für Patient {}. Benötigt: {} ETH.", session.getPatientName(), amountNeeded);
            Thread.sleep(1000);

            // SCHRITT 3: Ressourcen sammeln
            session.addLog("📦 Schritt 3: Ressourcen sammeln");
            // Use providers to get new instances for the lists
            VRTherapieModul newVrModul = vrTherapieModulProvider.get();
            vrTherapieModule.add(newVrModul);
            session.incrementVrModules();
            session.addLog("✅ VR-Therapie-Modul hinzugefügt");

            MuskelStimulator newMuskelStimulator = muskelStimulatorProvider.get();
            muskelStimulatoren.add(newMuskelStimulator);
            session.incrementEmsDevices();
            session.addLog("✅ Muskel-Stimulator hinzugefügt");

            TENSGeraet newTensGeraet = tensGeraetProvider.get();
            tensGeraete.add(newTensGeraet);
            session.incrementTensDevices();
            session.addLog("✅ TENS-Gerät hinzugefügt");
            log.info("SpineAidNetSystem: Ressourcen gesammelt für Patient {}. VR: {}, EMS: {}, TENS: {}. Current list sizes: VR={}, EMS={}, TENS={}",
                    session.getPatientName(), session.getVrModules(), session.getEmsDevices(), session.getTensDevices(),
                    vrTherapieModule.size(), muskelStimulatoren.size(), tensGeraete.size());
            Thread.sleep(1000);

            // SCHRITT 4: Patientendaten laden
            session.addLog("☁️ Schritt 4: Patientendaten laden");
            Patient patient = cloudDatenManager.ladePatientendaten(session);
            datenManager.speicherePatientendaten(patient, session);
            session.setPatientData(patient);
            log.info("SpineAidNetSystem: Patientendaten geladen und gespeichert für Patient {}. Diagnose: {}.", patient.getName(), patient.getDiagnose());
            Thread.sleep(1000);

            // SCHRITT 5: Behandlungsplan erstellen
            session.addLog("🤖 Schritt 5: KI erstellt Behandlungsplan");
            Behandlungsplan behandlungsplan = kiController.erstelleBehandlungsplan(patient, session);
            log.info("SpineAidNetSystem: Behandlungsplan erstellt für Patient {}. VR-Umgebung: {}.", patient.getName(), behandlungsplan.getVrUmgebung());
            Thread.sleep(1000);

            // SCHRITT 6: Sensoren aktivieren
            session.addLog("📡 Schritt 6: Sensoren aktivieren");
            sensorModul.ueberwachePatienten(patient, session);
            log.info("SpineAidNetSystem: Sensoren aktiviert für Patient {}.", patient.getPatientName());
            Thread.sleep(1000);

            // SCHRITT 7: Behandlung durchführen (3 For-Loops)
            session.addLog("💊 Schritt 7: Behandlung durchführen");
            log.info("SpineAidNetSystem: Starte Behandlung für Patient {}. Using {} VR modules, {} EMS, {} TENS.",
                    session.getPatientName(), vrTherapieModule.size(), muskelStimulatoren.size(), tensGeraete.size());

            for (VRTherapieModul modul : vrTherapieModule) {
                if (!session.isActive()) break;
                modul.starteTherapie(behandlungsplan, session);
                Thread.sleep(800);
            }

            for (MuskelStimulator stimulator : muskelStimulatoren) {
                if (!session.isActive()) break;
                stimulator.stimuliereMuskeln(behandlungsplan, session);
                Thread.sleep(800);
            }

            for (TENSGeraet geraet : tensGeraete) {
                if (!session.isActive()) break;
                geraet.beginneBehandlung(behandlungsplan, session);
                Thread.sleep(800);
            }
            log.info("SpineAidNetSystem: Behandlung abgeschlossen für Patient {}.", session.getPatientName());

            // SCHRITT 8: KI-Überwachung
            session.addLog("📊 Schritt 8: KI überwacht und passt an");
            kiController.ueberwacheUndPasseAn(patient, behandlungsplan, session);
            log.info("SpineAidNetSystem: KI-Überwachung und Anpassung abgeschlossen für Patient {}.", patient.getPatientName());
            Thread.sleep(1000);

            // SCHRITT 9: UI-Status anzeigen
            session.addLog("🖥️ Schritt 9: Status-Anzeige");
            ui.zeigeStatus(session);
            log.info("SpineAidNetSystem: UI-Status angezeigt für Patient {}.", session.getPatientName());
            Thread.sleep(500);

            session.addLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            session.addLog("✅ SPINEAIDNET ERFOLGREICH AKTIVIERT!");
            session.addLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            session.setStatus("ABGESCHLOSSEN");
            log.info("SpineAidNetSystem: System erfolgreich aktiviert für Patient {}.", session.getPatientName());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("SpineAidNetSystem: Systemaktivierung unterbrochen für Patient {}: {}", session.getPatientName(), e.getMessage());
            session.addLog("❌ SYSTEMAKTIVIERUNG UNTERBROCHEN: " + e.getMessage());
            session.setStatus("ABGEBROCHEN");
        } catch (Exception e) {
            log.error("SpineAidNetSystem: Fehler während der Systemaktivierung für Patient {}: {}", session.getPatientName(), e.getMessage(), e);
            session.addLog("❌ FEHLER: " + e.getMessage());
            ui.zeigeFehlermeldung("Systemfehler: " + e.getMessage(), session);
            session.setStatus("FEHLER");
        }
    }
}
