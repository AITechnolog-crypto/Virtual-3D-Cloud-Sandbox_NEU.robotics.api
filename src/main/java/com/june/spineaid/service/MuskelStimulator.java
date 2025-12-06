package com.june.spineaid.service;

import com.june.spineaid.model.Behandlungsplan;
import com.june.spineaid.model.SpineAidSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MuskelStimulator {
    public void stimuliereMuskeln(Behandlungsplan plan, SpineAidSession session) {
        session.addLog("💪 EMS Stimulation: " + plan.getEmsIntensitaet() + "%");
        session.incrementTreatmentsCompleted();
        double newPain = session.getCurrentPainLevel() * 0.80;
        session.setCurrentPainLevel(newPain);
        log.info("EMS Stimulation gestartet für Patient {}. Intensität: {}%. Neues Schmerzlevel: {}.", session.getPatientName(), plan.getEmsIntensitaet(), newPain);
    }
}
