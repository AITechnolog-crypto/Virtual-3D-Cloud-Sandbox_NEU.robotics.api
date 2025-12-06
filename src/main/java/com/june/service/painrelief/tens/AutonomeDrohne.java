package com.june.service.painrelief.tens;

import com.june.model.painrelief.Behandlungsplan;
import com.june.model.painrelief.Schmerzdaten;
import com.june.model.painrelief.tens.TherapySession;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@Slf4j
public class AutonomeDrohne {
    private String id;
    private boolean aktiv;
    private GPSNavigator gpsNavigator;
    private ComputerVision computerVision;
    private TENSGeraet tensGeraet;
    private MuskelStimulator muskelStimulator;

    public AutonomeDrohne(String id) {
        this.id = id;
        this.aktiv = false;
        this.gpsNavigator = new GPSNavigator();
        this.computerVision = new ComputerVision();
        this.tensGeraet = new TENSGeraet();
        this.muskelStimulator = new MuskelStimulator();
    }

    public void aktiviere(TherapySession session) {
        this.aktiv = true;
        session.addLog("🚁 Autonome Drohne " + id.substring(0, 8) + " aktiviert");
        log.info("Autonome Drohne {} activated.", id.substring(0, 8));

        // Aktiviere GPS
        gpsNavigator.aktiviere(session);

        // Aktiviere Computer Vision
        computerVision.aktiviere(session);

        session.addLog("✅ Alle Systeme online!");
    }

    public Schmerzdaten sammleDaten(TherapySession session) {
        session.addLog("📡 Sammle Vitaldaten...");
        log.info("Autonome Drohne {} collecting vital data.", id.substring(0, 8));

        // GPS-Position
        double[] position = gpsNavigator.getPosition();
        session.setGpsAccuracy(95 + Math.random() * 5);

        // Computer Vision Analyse
        String bodyRegion = computerVision.analyzeBodyRegion(session);

        double intensitaet = 3 + Math.random() * 7;
        String[] typen = {"Akut", "Chronisch", "Muskulär", "Nervlich"};
        String typ = typen[(int) (Math.random() * typen.length)];

        return new Schmerzdaten(intensitaet, bodyRegion, typ);
    }

    public void fuehreBehandlungAus(Behandlungsplan plan, TherapySession session) {
        try {
            session.addLog("💊 Behandlung wird durchgeführt...");
            log.info("Autonome Drohne {} executing treatment based on plan.", id.substring(0, 8));

            // TENS-Therapie
            if (plan.isTensEmpfohlen()) {
                tensGeraet.startTherapy(plan.getTensFrequenz(), plan.getTensIntensitaet(), session);
            }

            // Muskelstimulation
            if (plan.isEmsEmpfohlen()) {
                muskelStimulator.stimulate(plan.getEmsIntensitaet(), session);
            }

            Thread.sleep(1500);

            session.addLog("✅ Behandlung erfolgreich!");
            session.incrementTreatmentsCompleted();

            // Reduziere Schmerz
            double newPain = session.getCurrentPainLevel() * 0.7;
            session.setCurrentPainLevel(newPain);
            log.info("Autonome Drohne {} treatment successful. New pain level: {}", id.substring(0, 8), newPain);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            session.addLog("❌ Behandlung unterbrochen");
            log.warn("Autonome Drohne {} treatment interrupted: {}", id.substring(0, 8), e.getMessage());
        }
    }
}
