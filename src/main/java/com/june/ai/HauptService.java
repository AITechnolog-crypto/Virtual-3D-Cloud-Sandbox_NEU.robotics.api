package com.june.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.june.ai.service.AiAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Hauptprogramm für KI-gesteuerte Analyse und Optimierung (als Spring Service)
 *
 * Diese Klasse wurde von einer Standalone-Anwendung in einen Spring Service umgewandelt.
 * Die Analyse-Logik wird nun über die `performAnalysis`-Methode bereitgestellt.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HauptService {

    private final AiAnalysisService aiAnalysisService;

    /**
     * Führt eine KI-gesteuerte Analyse basierend auf den bereitgestellten Cloud-Daten durch.
     *
     * @param cloudDaten Eine Map mit Cloud-Daten zur Analyse.
     */
    public void performAnalysis(Map<String, Object> cloudDaten) {
        log.info("🧠 Starte KI-gesteuerte Analyse (via HauptService)...");
        aiAnalysisService.runCloudAnalysis(cloudDaten);
        log.info("✅ KI-gesteuerte Analyse (via HauptService) abgeschlossen.");
    }

    // Die ursprüngliche main-Methode und Argument-Parsing-Logik wurden entfernt,
    // da diese Klasse nun als Spring Service im Kontext der Anwendung läuft.
}
