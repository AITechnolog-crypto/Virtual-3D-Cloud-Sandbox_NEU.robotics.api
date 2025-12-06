package com.june.gcloud.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Ein zentraler Dienst zur Verwaltung und Rotation von API-Schlüsseln für Google-Dienste.
 * Er ist zustandslos für den Aufrufer, merkt sich aber intern den zuletzt verwendeten Schlüssel
 * für einen bestimmten Dienst, um eine faire Rotation zu gewährleisten.
 */
@Service
public class GoogleKeyRotationService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleKeyRotationService.class);

    // Eine Map, die für jeden Dienst (z.B. "gemini", "maps") den Index des nächsten zu verwendenden Schlüssels speichert.
    private final ConcurrentHashMap<String, AtomicInteger> keyIndexMap = new ConcurrentHashMap<>();

    /**
     * Holt den nächsten verfügbaren API-Schlüssel für einen bestimmten Dienst.
     * Wenn die Liste der Schlüssel leer ist, wird null zurückgegeben.
     *
     * @param serviceName Der Name des Dienstes (z.B. "gemini", "maps").
     * @param keys Die Liste der verfügbaren Schlüssel für diesen Dienst.
     * @return Der nächste zu verwendende API-Schlüssel oder null.
     */
    public String getNextKey(String serviceName, List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            logger.warn("Keine API-Schlüssel für den Dienst '{}' konfiguriert.", serviceName);
            return null;
        }

        // Hole den aktuellen Index für diesen Dienst. Wenn nicht vorhanden, starte bei 0.
        AtomicInteger currentIndex = keyIndexMap.computeIfAbsent(serviceName, k -> new AtomicInteger(0));

        // Inkrementiere den Index und hole den Wert. Modulo sorgt für einen Ringpuffer.
        int keyIndex = currentIndex.getAndIncrement() % keys.size();

        String selectedKey = keys.get(keyIndex);
        logger.info("Nächster Schlüssel für Dienst '{}' ist Index {}.", serviceName, keyIndex);

        return selectedKey;
    }

    /**
     * Setzt den Index für einen Dienst zurück, z.B. nach einem Konfigurations-Reload.
     */
    public void resetService(String serviceName) {
        keyIndexMap.remove(serviceName);
        logger.info("Schlüssel-Index für Dienst '{}' wurde zurückgesetzt.", serviceName);
    }
}
