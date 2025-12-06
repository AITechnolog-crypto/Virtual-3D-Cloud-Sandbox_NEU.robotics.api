package com.june.spineaid.service;

import com.june.spineaid.model.Behandlungsplan;
import com.june.spineaid.model.SpineAidSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VRTherapieModul {
    public void starteTherapie(Behandlungsplan plan, SpineAidSession session) {
        session.addLog("🥽 VR-Therapie gestartet: " + plan.getVrUmgebung());
        session.incrementTreatmentsCompleted();
        double newPain = session.getCurrentPainLevel() * 0.75;
        session.setCurrentPainLevel(newPain);
        log.info("VR Therapie gestartet für Patient {}. Umgebung: {}. Neues Schmerzlevel: {}.", session.getPatientName(), plan.getVrUmgebung(), newPain);
    }
}
