package com.june.service.painrelief;

import com.june.model.painrelief.Schmerzdaten;
import com.june.model.painrelief.TreatmentSession;
import lombok.extern.slf44j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class SchmerzAnalyseModul {

    public Schmerzdaten analysiereSchmerzen(Map<String, Object> daten, TreatmentSession session) {
        double intensitaet = 3 + Math.random() * 7; // 3-10
        String[] lokalisierungen = {"Lendenwirbel", "Brustwirbel", "Halswirbel", "Unterer Rücken"};
        String lokalisierung = lokalisierungen[(int) (Math.random() * lokalisierungen.length)];

        String[] typen = {"Akut", "Chronisch", "Muskulär", "Nervlich"};
        String typ = typen[(int) (Math.random() * typen.length)];

        log.info("Analysiere Schmerzen. Intensität: {}, Lokalisierung: {}, Typ: {}", intensitaet, lokalisierung, typ);
        return new Schmerzdaten(intensitaet, lokalisierung, typ);
    }

    public boolean istSchmerzGelindert(Schmerzdaten schmerzdaten, TreatmentSession session) {
        boolean relieved = schmerzdaten.getIntensitaet() < 3.0;
        log.info("Schmerz gelindert: {}. Aktuelle Intensität: {}", relieved, schmerzdaten.getIntensitaet());
        return relieved;
    }
}
