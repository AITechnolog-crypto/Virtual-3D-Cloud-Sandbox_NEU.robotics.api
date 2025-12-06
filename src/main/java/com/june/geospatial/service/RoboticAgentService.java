package com.june.geospatial.service;

import com.june.geospatial.entity.MissionFeedback;
import com.june.geospatial.entity.PlaceCache;
import com.june.geospatial.entity.RoboticAgent;
import com.june.geospatial.repository.MissionFeedbackRepository;
import com.june.geospatial.repository.PlaceCacheRepository;
import com.june.geospatial.repository.RoboticAgentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Service-Schicht zur Verwaltung der Logik von Roboter-Agenten.
 * Beinhaltet intelligente Entscheidungsfindung, Bewegungs-Simulation und das Protokollieren
 * von Erfolgen und Misserfolgen (Good/Bad Container).
 */
@Service
@RequiredArgsConstructor
public class RoboticAgentService {

    private static final Logger logger = LoggerFactory.getLogger(RoboticAgentService.class);
    private final RoboticAgentRepository agentRepository;
    private final PlaceCacheRepository placeCacheRepository;
    private final MissionFeedbackRepository feedbackRepository; // Das "Gedächtnis" für Aktionen

    private static final double MAX_SLOPE_DEGREES = 30.0;
    private static final double MOVEMENT_SPEED = 0.05;

    /**
     * Empfängt einen Bewegungsbefehl, analysiert die Machbarkeit und führt ihn aus oder lehnt ihn ab.
     */
    public void sendMoveCommand(String agentId, double targetLat, double targetLng) {
        agentRepository.findById(agentId).ifPresentOrElse(agent -> {
            logger.info("🧠 Agent {} analysiert Zielkoordinaten ({}, {}) über die Wissensdatenbank...", agentId, targetLat, targetLng);

            Optional<PlaceCache> nearestTile = findNearestWorldTile(targetLat, targetLng);

            if (nearestTile.isPresent()) {
                PlaceCache tile = nearestTile.get();
                logger.info("   -> Relevante Welt-Kachel gefunden: {}", tile.getName());

                boolean isTraversable = parseJson(tile.getAdditionalData(), "isTraversable", true);
                double slope = parseJson(tile.getAdditionalData(), "slopeAngle_degrees", 0.0);

                if (!isTraversable) {
                    String reason = "Ziel ist laut Wissensdatenbank als NICHT BEFAHRBAR markiert.";
                    rejectMoveCommand(agent, targetLat, targetLng, reason);
                    return;
                }
                if (slope > MAX_SLOPE_DEGREES) {
                    String reason = String.format("Ziel-Steigung von %.1f° überschreitet das Hardware-Limit von %.1f°.", slope, MAX_SLOPE_DEGREES);
                    rejectMoveCommand(agent, targetLat, targetLng, reason);
                    return;
                }
                logger.info("   -> Physikalische Analyse erfolgreich: Ziel ist sicher und befahrbar.");
            } else {
                logger.warn("   -> Keine spezifischen Physik-Daten für das Ziel gefunden. Fahre auf eigenes Risiko.");
            }

            acceptMoveCommand(agent, targetLat, targetLng);

        }, () -> logger.error("Agent mit ID {} nicht gefunden.", agentId));
    }

    private void acceptMoveCommand(RoboticAgent agent, double targetLat, double targetLng) {
        agent.setTargetLat(targetLat);
        agent.setTargetLng(targetLng);
        agent.setStatus("MOVING_TO_TARGET");
        agent.setMission("Bewege zu neuen Koordinaten: " + String.format("%.4f, %.4f", targetLat, targetLng));
        agent.setLastUpdate(Instant.now());
        agentRepository.save(agent);
        logger.info("✅ BEFEHL AKZEPTIERT für Agent {}. Beginne Bewegung.", agent.getAgentId());
    }

    private void rejectMoveCommand(RoboticAgent agent, double targetLat, double targetLng, String reason) {
        agent.setMission("BEFEHL VERWEIGERT: " + reason);
        agent.setStatus("IDLE");
        agentRepository.save(agent);
        logger.error("❌ BEFEHL VERWEIGERT für Agent {}: {}", agent.getAgentId(), reason);

        // --- LERNEN: Schreibe den Misserfolg in den "Bad Container" ---
        MissionFeedback feedback = new MissionFeedback(agent.getAgentId(), "FAILURE_REJECTED", reason, targetLat, targetLng);
        feedbackRepository.save(feedback);
        logger.info("   -> ✍️ Misserfolg im Feedback-System (Bad Container) protokolliert.");
    }

    @Scheduled(fixedRate = 2000)
    public void updateAgentPositions() {
        List<RoboticAgent> activeAgents = agentRepository.findAllByStatus("MOVING_TO_TARGET");
        if (activeAgents.isEmpty()) return;

        for (RoboticAgent agent : activeAgents) {
            double currentLat = agent.getCurrentLat();
            double currentLng = agent.getCurrentLng();
            double targetLat = agent.getTargetLat();
            double targetLng = agent.getTargetLng();

            double distance = Math.hypot(targetLat - currentLat, targetLng - currentLng);

            if (distance < MOVEMENT_SPEED) {
                agent.setCurrentLat(targetLat);
                agent.setCurrentLng(targetLng);
                agent.setStatus("IDLE");
                agent.setMission("Ziel erreicht. Warte auf neue Befehle.");
                logger.info("✅ Agent {} hat sein Ziel erreicht.", agent.getAgentId());

                // --- LERNEN: Schreibe den Erfolg in den "Good Container" ---
                MissionFeedback feedback = new MissionFeedback(agent.getAgentId(), "SUCCESS", "Ziel erfolgreich erreicht", targetLat, targetLng);
                feedbackRepository.save(feedback);
                logger.info("   -> ✍️ Erfolg im Feedback-System (Good Container) protokolliert.");

            } else {
                double dirLat = (targetLat - currentLat) / distance;
                double dirLng = (targetLng - currentLng) / distance;
                agent.setCurrentLat(currentLat + dirLat * MOVEMENT_SPEED);
                agent.setCurrentLng(currentLng + dirLng * MOVEMENT_SPEED);
            }
            agent.setLastUpdate(Instant.now());
            agentRepository.save(agent);
        }
    }

    private Optional<PlaceCache> findNearestWorldTile(double lat, double lng) {
        return placeCacheRepository.findAll().stream()
            .filter(t -> t.getLat() != 0 && t.getLng() != 0)
            .min((t1, t2) -> Double.compare(Math.hypot(t1.getLat() - lat, t1.getLng() - lng), Math.hypot(t2.getLat() - lat, t2.getLng() - lng)));
    }

    // Hilfsmethoden zum Parsen des JSON-Strings (vereinfacht)
    private boolean parseJson(String json, String key, boolean defaultValue) {
        if (json == null || !json.contains(key)) return defaultValue;
        try {
            String searchKey = "\"" + key + "\": "; // Korrigiert
            int keyIndex = json.indexOf(searchKey);
            if (keyIndex == -1) return defaultValue;
            String valuePart = json.substring(keyIndex + searchKey.length());
            int endIndex = valuePart.indexOf(',');
            if (endIndex == -1) {
                endIndex = valuePart.indexOf('}');
            }
            if (endIndex != -1) {
                valuePart = valuePart.substring(0, endIndex);
            }
            return valuePart.trim().startsWith("true");
        } catch (Exception e) {
            logger.warn("Fehler beim Parsen des JSON-Boolean-Werts für Schlüssel '{}' aus '{}'. Verwende Standardwert {}. Fehler: {}", key, json, defaultValue, e.getMessage());
            return defaultValue;
        }
    }

    private double parseJson(String json, String key, double defaultValue) {
        if (json == null || !json.contains(key)) return defaultValue;
        try {
            String searchKey = "\"" + key + "\": "; // Korrigiert
            int keyIndex = json.indexOf(searchKey);
            if (keyIndex == -1) return defaultValue;
            String valueStr = json.substring(keyIndex + searchKey.length()).split("[},]", 2)[0].trim();
            return Double.parseDouble(valueStr);
        } catch (NumberFormatException e) {
            logger.warn("Fehler beim Parsen des JSON-Double-Werts für Schlüssel '{}' aus '{}'. Ungültiges Zahlenformat. Verwende Standardwert {}. Fehler: {}", key, json, defaultValue, e.getMessage());
            return defaultValue;
        } catch (Exception e) { // Catch other potential parsing errors like StringIndexOutOfBoundsException
            logger.warn("Unerwarteter Fehler beim Parsen des JSON-Double-Werts für Schlüssel '{}' aus '{}'. Verwende Standardwert {}. Fehler: {}", key, json, defaultValue, e.getMessage());
            return defaultValue;
        }
    }
}
