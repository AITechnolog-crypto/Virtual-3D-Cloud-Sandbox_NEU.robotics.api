package com.june.ai.service;

import com.june.ai.CloudDatenKI;
import com.june.ai.FehlerErkennungsModul;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AiAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AiAnalysisService.class);

    private final FehlerErkennungsModul fehlerErkennungsModul;
    private final CloudDatenKI cloudDatenKI;

    public AiAnalysisService(FehlerErkennungsModul fehlerErkennungsModul, CloudDatenKI cloudDatenKI) {
        this.fehlerErkennungsModul = fehlerErkennungsModul;
        this.cloudDatenKI = cloudDatenKI;
    }

    public void runCloudAnalysis(Map<String, Object> cloudDaten) {
        log.info("🧠 Starte KI-gesteuerte Cloud-Analyse...");

        // Analyse starten
        cloudDatenKI.analysiereDatenUndGibErkenntnisseZurueck(cloudDaten);

        // Fehleranalyse modifizieren und Überwachung durchführen
        fehlerErkennungsModul.analyseDatenUndMarkiereFehler(cloudDaten);
        cloudDatenKI.ueberwacheUndKorrigiere();

        log.info("✅ KI-gesteuerte Cloud-Analyse abgeschlossen.");
    }
}
