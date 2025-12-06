package com.june.geospatial.service;

import com.june.gcloud.service.AdSenseService;
import com.june.gcloud.service.GeminiService;
import com.june.geospatial.dto.Place;
import com.june.geospatial.dto.PlacesNearbyResponse;
import com.june.geospatial.entity.PlaceCache;
import com.june.geospatial.repository.PlaceCacheRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.file.Path;
import java.util.List;

/**
 * Ein autonomer Sales Agent, der auf Basis von Marktanalysen personalisierte Verkaufs-E-Mails generiert und versendet.
 */
@Service
@RequiredArgsConstructor
public class SalesAgent implements IAgent {

    private static final Logger logger = LoggerFactory.getLogger(SalesAgent.class);
    private static final String AGENT_NAME = "Herr Gogo";

    private final PlaceCacheRepository placeCacheRepository;
    private final GeminiService geminiService;
    private final EmailService emailService; // Simulierter E-Mail-Service
    private final GeospatialService geospatialService;
    private final GoogleTrendsService trendsService;
    private final PresentationService presentationService;
    private final TextToSpeechService textToSpeechService;
    private final CalendarService calendarService;
    private final AgentCommunicationBus communicationBus;
    private final AdSenseService adSenseService;

    @PostConstruct
    public void registerAgent() {
        communicationBus.register(this);
    }

    @Override
    public String getAgentName() {
        return AGENT_NAME;
    }

    @Override
    public void receiveMessage(String sender, String message) {
        logger.info("Nachricht von '{}' erhalten: {}", sender, message);
        // Hier könnte der Agent auf Nachrichten von anderen Agenten reagieren
    }

    /**
     * Führt eine Verkaufs-Kampagne durch, basierend auf den neuesten Marktanalysen.
     */
    public void executeSalesCampaign() {
        logger.info("💰 Agent '{}' startet eine neue Verkaufs-Kampagne...", AGENT_NAME);

        // AdSense-Bericht abrufen
        String adSenseReport = adSenseService.getAdSenseReport();
        logger.info("   -> AdSense-Bericht erhalten: {}", adSenseReport);

        // Finde alle Marktanalysen mit hohem Potenzial
        List<PlaceCache> opportunities = placeCacheRepository.findAll().stream()
                .filter(e -> "Wirtschaft".equals(e.getDiscipline()) && e.getRelevanceScore() > 0.9)
                .toList();

        if (opportunities.isEmpty()) {
            logger.info("   -> Keine hochkarätigen Marktpotenziale für die aktuelle Kampagne gefunden.");
            return;
        }

        logger.info("   -> {} hochkarätige Marktpotenziale gefunden. Generiere personalisierte Ansprachen...", opportunities.size());

        for (PlaceCache opportunity : opportunities) {
            // Finde detaillierte Informationen über den potenziellen Kunden
            PlacesNearbyResponse places = geospatialService.findPlacesNearby(opportunity.getLat(), opportunity.getLng(), 100, opportunity.getConcept());
            if (places != null && places.getResults() != null && !places.getResults().isEmpty()) {
                Place customer = places.getResults().get(0);

                // Analysiere Markttrends
                String trends = trendsService.getInterestOverTime(opportunity.getConcept());
                logger.info("   -> Markttrends für '{}': {}", opportunity.getConcept(), trends);

                // Erstelle eine Präsentation
                Path presentation = presentationService.createPresentation("Partnerschaftsanfrage: " + opportunity.getConcept(), opportunity.getSummary());

                // Generiere eine personalisierte Verkaufs-E-Mail mit Gemini
                String prompt = String.format(
                    "Erstelle eine kurze, überzeugende Verkaufs-E-Mail an einen potenziellen Kunden. Das Thema ist: '%s'. Die Analyse hat ergeben: '%s'. Der Kunde ist '%s' in '%s'. Halte die E-Mail professionell und wecke Interesse an einer Partnerschaft. Erwähne die beigefügte Präsentation und schlage einen Termin für ein Google Meet vor.",
                    opportunity.getConcept(),
                    opportunity.getSummary(),
                    customer.getName(),
                    customer.getVicinity()
                );

                String emailBody = geminiService.getCompletion(prompt);

                // Generiere eine Sprachnachricht
                byte[] speech = textToSpeechService.synthesizeSpeech(emailBody);
                logger.info("   -> Sprachnachricht generiert ({} bytes)", speech.length);

                // Erstelle einen Google Meet-Termin
                String meetingLink = calendarService.createMeeting("Partnerschaftsanfrage: " + opportunity.getConcept(), emailBody, "2025-12-01T10:00:00-07:00");
                logger.info("   -> Google Meet-Termin erstellt: {}", meetingLink);

                // Versende die E-Mail (simuliert)
                String recipient = "ceo@" + customer.getName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase() + ".com";
                emailService.sendEmail(recipient, "Partnerschaftsanfrage: " + opportunity.getConcept(), emailBody + "\n\nPräsentation: " + presentation.toAbsolutePath() + "\n\nGoogle Meet-Termin: " + meetingLink);

                // Benachrichtige andere Agenten über die Kampagne
                communicationBus.sendMessage(AGENT_NAME, "ArchitectAgentService", "Sales campaign initiated for " + customer.getName() + " regarding " + opportunity.getConcept());
            }
        }

        logger.info("💰 Agent '{}' hat die Verkaufs-Kampagne abgeschlossen.", AGENT_NAME);
    }
}