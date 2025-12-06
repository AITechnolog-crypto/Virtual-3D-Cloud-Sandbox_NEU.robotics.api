package com.june.ai.notes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * KI-System für Notizen-Analyse und Vervollständigung
 * Mit MongoDB-Persistenz und Internet-Suche
 * NurSystem/JuneApplication Integration
 */
@Service
@ConditionalOnProperty(prefix = "ai.notes", name = "enabled", havingValue = "true")
public class NotesAiService {

    private static final Logger logger = LoggerFactory.getLogger(NotesAiService.class);

    @Autowired
    private NotesAiConfig config;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private EnvironmentalDataRepository environmentalDataRepository;

    @Autowired
    private CloudDataManager cloudDataManager;

    @Autowired
    private InternetSearchEngine searchEngine;

    /**
     * Analysiert und vervollständigt alle Notizen
     */
    public void analyzeAndCompleteNotes() {
        logger.info("Starte KI-Analyse aller Notizen");

        try {
            // 1. Lade alle Notizen aus Cloud und MongoDB
            List<NoteDocument> cloudNotes = cloudDataManager.fetchAllNotes();
            List<NoteDocument> localNotes = noteRepository.findAll();

            // 2. Merge und dedupliziere
            Map<String, NoteDocument> allNotes = mergeNotes(cloudNotes, localNotes);

            // 3. Analysiere jede Notiz
            int processed = 0;
            int completed = 0;

            for (NoteDocument note : allNotes.values()) {
                try {
                    if (note.hasMissingLogic()) {
                        logger.debug("Analysiere Notiz: {}", note.getNoteId());

                        // Keywords extrahieren
                        List<String> keywords = extractMissingLogicKeywords(note);
                        note.setMissingLogicKeywords(keywords);

                        // Internet-Suche
                        if (config.getAnalysis().isAutoComplete() && !keywords.isEmpty()) {
                            String missingInformation = searchEngine.findInformation(keywords);

                            if (missingInformation != null && !missingInformation.trim().isEmpty()) {
                                note.completeLogic(missingInformation);
                                completed++;
                                logger.info("Notiz vervollständigt: {}", note.getNoteId());
                            }
                        }

                        // KI-Analyse durchführen
                        performAiAnalysis(note);
                    }

                    // Umwelt-Check (falls aktiviert)
                    if (config.getAnalysis().isEnableEnvironmentalCheck()) {
                        checkEnvironmentalRelevance(note);
                    }

                    processed++;

                } catch (Exception e) {
                    logger.error("Fehler bei Notiz-Analyse: {}", note.getNoteId(), e);
                }
            }

            // 4. Speichere alle Notizen
            List<NoteDocument> notesToSave = new ArrayList<>(allNotes.values());
            if (config.getMongo().isEnabled()) {
                noteRepository.saveAll(notesToSave);
            }
            cloudDataManager.saveAllNotes(notesToSave);

            logger.info("KI-Analyse abgeschlossen: {} verarbeitet, {} vervollständigt", processed, completed);

        } catch (Exception e) {
            logger.error("Fehler bei KI-Analyse", e);
            throw new RuntimeException("KI-Analyse fehlgeschlagen", e);
        }
    }

    /**
     * Asynchrone Notiz-Analyse
     */
    @Async
    public CompletableFuture<NoteDocument> analyzeNoteAsync(String noteId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Optional<NoteDocument> noteOpt = noteRepository.findByNoteId(noteId);
                if (noteOpt.isPresent()) {
                    NoteDocument note = noteOpt.get();

                    if (note.hasMissingLogic()) {
                        List<String> keywords = extractMissingLogicKeywords(note);
                        String missingInfo = searchEngine.findInformation(keywords);
                        note.completeLogic(missingInfo);
                        performAiAnalysis(note);

                        return noteRepository.save(note);
                    }
                    return note;
                } else {
                    throw new RuntimeException("Notiz nicht gefunden: " + noteId);
                }
            } catch (Exception e) {
                logger.error("Async Notiz-Analyse fehlgeschlagen: {}", noteId, e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Extrahiert Keywords für fehlende Logik
     */
    private List<String> extractMissingLogicKeywords(NoteDocument note) {
        List<String> keywords = new ArrayList<>();

        if (note.getContent() == null || note.getContent().trim().isEmpty()) {
            return keywords;
        }

        String content = note.getContent().toLowerCase();

        // Fragezeichen und unvollständige Sätze finden
        Pattern questionPattern = Pattern.compile("\\b(was|wie|wann|wo|warum|welche|welcher|welches)\\b.*?\\?");
        Matcher questionMatcher = questionPattern.matcher(content);
        while (questionMatcher.find()) {
            String question = questionMatcher.group().trim();
            keywords.addAll(extractKeywordsFromText(question));
        }

        // Unvollständige Marker
        List<String> incompleteMarkers = Arrays.asList(
            "todo", "fixme", "xxx", "???", "...", "unvollständig",
            "fehlt", "ergänzen", "recherchieren", "klären"
        );

        for (String marker : incompleteMarkers) {
            if (content.contains(marker)) {
                keywords.add(marker);
                // Kontext um Marker extrahieren
                int index = content.indexOf(marker);
                int start = Math.max(0, index - 50);
                int end = Math.min(content.length(), index + 50);
                String context = content.substring(start, end);
                keywords.addAll(extractKeywordsFromText(context));
            }
        }

        // Fachbegriffe extrahieren
        keywords.addAll(extractTechnicalTerms(content));

        // Limit auf max Keywords
        int maxKeywords = config.getAnalysis().getMaxKeywords();
        if (keywords.size() > maxKeywords) {
            keywords = keywords.subList(0, maxKeywords);
        }

        logger.debug("Extrahierte Keywords für {}: {}", note.getNoteId(), keywords);
        return keywords;
    }

    /**
     * Extrahiert Keywords aus Text
     */
    private List<String> extractKeywordsFromText(String text) {
        List<String> keywords = new ArrayList<>();

        // Stopwords entfernen
        Set<String> stopwords = Set.of(
            "der", "die", "das", "und", "oder", "aber", "mit", "von", "zu", "in", "auf", "für", "ist", "sind", "war", "waren"
        );

        String[] words = text.replaceAll("[^a-zA-ZäöüÄÖÜß\\s]", "").split("\\s+");

        for (String word : words) {
            word = word.trim().toLowerCase();
            if (word.length() > 3 && !stopwords.contains(word)) {
                keywords.add(word);
            }
        }

        return keywords;
    }

    /**
     * Extrahiert technische Begriffe
     */
    private List<String> extractTechnicalTerms(String content) {
        List<String> terms = new ArrayList<>();

        // Technische Patterns
        Pattern[] techPatterns = {
            Pattern.compile("\\b[A-Z]{2,}\\b"), // Akronyme
            Pattern.compile("\\b\\w+\\.\\w+\\b"), // Dateierweiterungen/Domains
            Pattern.compile("\\b\\d+\\.\\d+\\b"), // Versionsnummern
            Pattern.compile("\\b[a-zA-Z]+\\d+\\b") // Alphanumerische Codes
        };

        for (Pattern pattern : techPatterns) {
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                terms.add(matcher.group());
            }
        }

        return terms;
    }

    /**
     * Führt KI-Analyse durch
     */
    private void performAiAnalysis(NoteDocument note) {
        NoteDocument.AnalysisResult analysis = new NoteDocument.AnalysisResult();

        // Completion Score berechnen
        double completionScore = calculateCompletionScore(note);
        note.updateCompletionScore(completionScore);

        // Analyse-Typ bestimmen
        String analysisType = determineAnalysisType(note);
        analysis.setAnalysisType(analysisType);

        // Confidence berechnen
        double confidence = calculateConfidence(note);
        analysis.setConfidence(confidence);

        // Empfehlungen generieren
        String recommendation = generateRecommendation(note);
        analysis.setRecommendation(recommendation);

        // Suggested Keywords
        List<String> suggestedKeywords = generateSuggestedKeywords(note);
        analysis.setSuggestedKeywords(suggestedKeywords);

        note.setAnalysisResult(analysis);

        logger.debug("KI-Analyse für {}: Score={}, Confidence={}, Type={}",
            note.getNoteId(), completionScore, confidence, analysisType);
    }

    /**
     * Berechnet Completion Score
     */
    private double calculateCompletionScore(NoteDocument note) {
        if (note.getContent() == null) return 0.0;

        String content = note.getContent();
        double score = 0.0;

        // Länge (max 30 Punkte)
        int length = content.length();
        score += Math.min(30.0, length / 10.0);

        // Vollständige Sätze (max 25 Punkte)
        long sentences = content.chars().filter(ch -> ch == '.' || ch == '!' || ch == '?').count();
        score += Math.min(25.0, sentences * 5.0);

        // Keine Fragezeichen/Unvollständigkeiten (max 20 Punkte)
        if (!content.contains("???") && !content.contains("...") && !content.contains("TODO")) {
            score += 20.0;
        }

        // Strukturierung (max 15 Punkte)
        if (content.contains("\n") || content.contains("-") || content.contains("*")) {
            score += 15.0;
        }

        // Zusätzliche Informationen (max 10 Punkte)
        if (note.getAdditionalInfo() != null && !note.getAdditionalInfo().isEmpty()) {
            score += 10.0;
        }

        return Math.min(100.0, score) / 100.0;
    }

    /**
     * Bestimmt Analyse-Typ
     */
    private String determineAnalysisType(NoteDocument note) {
        String content = note.getContent().toLowerCase();

        if (content.contains("umwelt") || content.contains("environmental")) {
            return "ENVIRONMENTAL";
        } else if (content.contains("technik") || content.contains("technical")) {
            return "TECHNICAL";
        } else if (content.contains("business") || content.contains("geschäft")) {
            return "BUSINESS";
        } else if (content.contains("research") || content.contains("forschung")) {
            return "RESEARCH";
        } else {
            return "GENERAL";
        }
    }

    /**
     * Berechnet Confidence
     */
    private double calculateConfidence(NoteDocument note) {
        double confidence = note.getCompletionScore();

        // Reduziere Confidence bei vielen fehlenden Keywords
        if (note.getMissingLogicKeywords().size() > 5) {
            confidence *= 0.8;
        }

        // Erhöhe Confidence bei zusätzlichen Informationen
        if (note.getAdditionalInfo() != null && !note.getAdditionalInfo().isEmpty()) {
            confidence = Math.min(1.0, confidence * 1.2);
        }

        return confidence;
    }

    /**
     * Generiert Empfehlung
     */
    private String generateRecommendation(NoteDocument note) {
        StringBuilder rec = new StringBuilder();

        if (note.getCompletionScore() < 0.5) {
            rec.append("Notiz ist unvollständig - weitere Details erforderlich. ");
        }

        if (note.getMissingLogicKeywords().size() > 3) {
            rec.append("Viele offene Punkte - systematische Recherche empfohlen. ");
        }

        if (note.getContent() != null && note.getContent().contains("???")) {
            rec.append("Offene Fragen klären. ");
        }

        if (rec.length() == 0) {
            rec.append("Notiz ist gut strukturiert und vollständig.");
        }

        return rec.toString().trim();
    }

    /**
     * Generiert vorgeschlagene Keywords
     */
    private List<String> generateSuggestedKeywords(NoteDocument note) {
        List<String> suggested = new ArrayList<>();

        String analysisType = note.getAnalysisResult().getAnalysisType();

        switch (analysisType) {
            case "ENVIRONMENTAL":
                suggested.addAll(Arrays.asList("umweltschutz", "nachhaltigkeit", "ökologie", "klimawandel"));
                break;
            case "TECHNICAL":
                suggested.addAll(Arrays.asList("technologie", "innovation", "entwicklung", "software"));
                break;
            case "BUSINESS":
                suggested.addAll(Arrays.asList("strategie", "marketing", "finanzen", "wachstum"));
                break;
            case "RESEARCH":
                suggested.addAll(Arrays.asList("studie", "analyse", "methodik", "ergebnisse"));
                break;
            default:
                suggested.addAll(Arrays.asList("information", "details", "kontext", "hintergrund"));
        }

        return suggested.subList(0, Math.min(5, suggested.size()));
    }

    /**
     * Prüft Umwelt-Relevanz
     */
    private void checkEnvironmentalRelevance(NoteDocument note) {
        String content = note.getContent().toLowerCase();

        List<String> envKeywords = Arrays.asList(
            "umwelt", "environmental", "nachhaltigkeit", "sustainability",
            "ökologie", "ecology", "klimawandel", "climate", "biodiversität"
        );

        boolean isEnvironmentallyRelevant = envKeywords.stream()
            .anyMatch(content::contains);

        if (isEnvironmentallyRelevant) {
            // Erstelle oder aktualisiere Umweltdaten
            EnvironmentalDataDocument envData = createEnvironmentalDataFromNote(note);
            if (envData != null) {
                environmentalDataRepository.save(envData);
                logger.info("Umweltdaten erstellt für Notiz: {}", note.getNoteId());
            }
        }
    }

    /**
     * Erstellt Umweltdaten aus Notiz
     */
    private EnvironmentalDataDocument createEnvironmentalDataFromNote(NoteDocument note) {
        // Vereinfachte Extraktion - in Realität würde hier NLP verwendet
        EnvironmentalDataDocument envData = new EnvironmentalDataDocument();
        envData.setDataId(UUID.randomUUID().toString());
        envData.setLocation("Extracted from Note: " + note.getNoteId());
        envData.setDataSource("AI_NOTE_ANALYSIS");

        // Default-Werte basierend auf Notiz-Inhalt
        String content = note.getContent().toLowerCase();

        envData.setProtectedArea(content.contains("schutzgebiet") || content.contains("protected"));
        envData.setWillAffectBiodiversity(content.contains("biodiversität") || content.contains("biodiversity"));
        envData.setSoilQualityIndex(content.contains("boden") ? 60.0 : 50.0);
        envData.setHasWaterSources(content.contains("wasser") || content.contains("water"));
        envData.setVegetationDensity(content.contains("vegetation") || content.contains("pflanzen") ? 75.0 : 50.0);

        return envData;
    }

    /**
     * Merged Notizen aus verschiedenen Quellen
     */
    private Map<String, NoteDocument> mergeNotes(List<NoteDocument> cloudNotes, List<NoteDocument> localNotes) {
        Map<String, NoteDocument> merged = new HashMap<>();

        // Lokale Notizen zuerst
        for (NoteDocument note : localNotes) {
            if (note.getNoteId() != null) {
                merged.put(note.getNoteId(), note);
            }
        }

        // Cloud-Notizen (überschreiben bei neuerer Version)
        for (NoteDocument cloudNote : cloudNotes) {
            if (cloudNote.getNoteId() != null) {
                NoteDocument existing = merged.get(cloudNote.getNoteId());
                if (existing == null ||
                    (cloudNote.getUpdatedAt() != null && existing.getUpdatedAt() != null &&
                     cloudNote.getUpdatedAt().isAfter(existing.getUpdatedAt()))) {
                    merged.put(cloudNote.getNoteId(), cloudNote);
                }
            }
        }

        return merged;
    }

    /**
     * Erstellt neue Notiz
     */
    public NoteDocument createNote(String content, String owner, String category) {
        NoteDocument note = new NoteDocument(content);
        note.setNoteId(UUID.randomUUID().toString());
        note.setOwner(owner);
        note.setCategory(category);
        note.setPriority("MEDIUM");

        // Initiale Analyse
        performAiAnalysis(note);

        if (config.getMongo().isEnabled()) {
            note = noteRepository.save(note);
        }

        logger.info("Neue Notiz erstellt: {}", note.getNoteId());
        return note;
    }

    /**
     * Health Check
     */
    public boolean isHealthy() {
        try {
            return cloudDataManager.isAvailable() &&
                   searchEngine.isAvailable() &&
                   config.getMongo().isEnabled();
        } catch (Exception e) {
            logger.error("Health Check fehlgeschlagen", e);
            return false;
        }
    }
}
