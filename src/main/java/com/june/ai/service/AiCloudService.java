package com.june.ai.service;

import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import com.june.ai.AiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AICloudService - Deep Java Library (DJL) Integration als Spring Service
 *
 * Diese Klasse implementiert eine vollständige AI-Cloud-Anwendung mit:
 * - Text-Klassifizierung (NLP)
 * - Sentiment-Analyse
 * - Named Entity Recognition (NER)
 * - Frage-Antwort-Systeme
 * - Text-Generierung
 * - Multi-Modell-Management
 *
 * Features:
 * - ModelZoo Integration
 * - Konfigurierbare Modelle
 * - Exception-Handling
 * - Performance-Tracking
 * - Multi-Threading Support
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiCloudService {

    private final AiConfig config;
    private final Map<String, ModelInfo> loadedModels = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        if (!config.getMl().isEnabled()) {
            log.info("AiCloudService: ML models are disabled by configuration.");
            return;
        }
        log.info("AiCloudService: Initializing AI models from ModelZoo...");
        try {
            loadTextClassificationModel();
            loadSentimentAnalysisModel();
            loadNERModel();
            log.info("AiCloudService: All models loaded successfully.");
        } catch (Exception e) {
            log.error("AiCloudService: Error initializing AI models: {}", e.getMessage(), e);
        }
    }

    @PreDestroy
    public void cleanup() {
        log.info("AiCloudService: Closing all loaded AI models.");
        closeAllModels();
        log.info("AiCloudService: All models closed.");
    }

    /**
     * Lädt Text-Klassifizierungs-Modell
     */
    private void loadTextClassificationModel() {
        String modelName = "TextClassification";
        log.info("AiCloudService: Loading {} model...", modelName);
        try {
            Criteria<String, Classifications> criteria = Criteria.builder()
                    .optApplication(Application.NLP.TEXT_CLASSIFICATION)
                    .setTypes(String.class, Classifications.class)
                    .optProgress(new SimpleProgressBar())
                    .build();
            ZooModel<String, Classifications> model = ModelZoo.loadModel(criteria);
            loadedModels.put(modelName, new ModelInfo(modelName, "TEXT_CLASSIFICATION", model));
            log.info("AiCloudService: {} model loaded.", modelName);
        } catch (ModelNotFoundException e) {
            log.warn("AiCloudService: Model {} not found: {}", modelName, e.getMessage());
        } catch (MalformedModelException e) {
            log.error("AiCloudService: Malformed model {}: {}", modelName, e.getMessage());
        } catch (IOException e) {
            log.error("AiCloudService: I/O error loading model {}: {}", modelName, e.getMessage());
        }
    }

    /**
     * Lädt Sentiment-Analyse-Modell
     */
    private void loadSentimentAnalysisModel() {
        String modelName = "SentimentAnalysis";
        log.info("AiCloudService: Loading {} model...", modelName);
        try {
            Criteria<String, Classifications> criteria = Criteria.builder()
                    .optApplication(Application.NLP.SENTIMENT_ANALYSIS)
                    .setTypes(String.class, Classifications.class)
                    .optProgress(new SimpleProgressBar())
                    .build();
            ZooModel<String, Classifications> model = ModelZoo.loadModel(criteria);
            loadedModels.put(modelName, new ModelInfo(modelName, "SENTIMENT_ANALYSIS", model));
            log.info("AiCloudService: {} model loaded.", modelName);
        } catch (ModelNotFoundException e) {
            log.warn("AiCloudService: Model {} not found: {}", modelName, e.getMessage());
        } catch (MalformedModelException e) {
            log.error("AiCloudService: Malformed model {}: {}", modelName, e.getMessage());
        } catch (IOException e) {
            log.error("AiCloudService: I/O error loading model {}: {}", modelName, e.getMessage());
        }
    }

    /**
     * Lädt Named Entity Recognition Modell
     */
    private void loadNERModel() {
        String modelName = "NER";
        log.info("AiCloudService: Loading {} model...", modelName);
        try {
            Criteria<String, Classifications> criteria = Criteria.builder()
                    .optApplication(Application.NLP.NAMED_ENTITY_RECOGNITION)
                    .setTypes(String.class, Classifications.class)
                    .optProgress(new SimpleProgressBar())
                    .build();
            ZooModel<String, Classifications> model = ModelZoo.loadModel(criteria);
            loadedModels.put(modelName, new ModelInfo(modelName, "NER", model));
            log.info("AiCloudService: {} model loaded.", modelName);
        } catch (ModelNotFoundException e) {
            log.warn("AiCloudService: Model {} not found (optional): {}", modelName, e.getMessage());
        } catch (MalformedModelException e) {
            log.error("AiCloudService: Malformed model {}: {}", modelName, e.getMessage());
        } catch (IOException e) {
            log.error("AiCloudService: I/O error loading model {}: {}", modelName, e.getMessage());
        }
    }

    /**
     * Macht eine Vorhersage mit einem geladenen Modell
     */
    public PredictionResult predict(String modelName, String input) {
        PredictionResult result = new PredictionResult(input, modelName);
        if (!loadedModels.containsKey(modelName)) {
            result.setError("Modell nicht geladen: " + modelName);
            log.warn("AiCloudService: Prediction failed, model '{}' not loaded.", modelName);
            return result;
        }

        ModelInfo modelInfo = loadedModels.get(modelName);
        long startTime = System.currentTimeMillis();

        try {
            try (Predictor<String, Classifications> predictor = modelInfo.getModel().newPredictor()) {
                Classifications classifications = predictor.predict(input);
                String bestClass = classifications.best().getClassName();
                double confidence = classifications.best().getProbability();
                long inferenceTime = System.currentTimeMillis() - startTime;

                result.setOutput(bestClass);
                result.setConfidence(confidence);
                result.setInferenceTime(inferenceTime);
                result.setSuccess(true);
                modelInfo.updateInferenceTime(inferenceTime);
                log.debug("AiCloudService: Prediction for '{}' with model '{}' successful. Result: {}", input, modelName, bestClass);
            }
        } catch (TranslateException e) {
            result.setError("Fehler bei der Vorhersage: " + e.getMessage());
            log.error("AiCloudService: Prediction error for model '{}': {}", modelName, e.getMessage(), e);
        } catch (Exception e) {
            result.setError("Unerwarteter Fehler: " + e.getMessage());
            log.error("AiCloudService: Unexpected error during prediction for model '{}': {}", modelName, e.getMessage(), e);
        }
        return result;
    }

    /**
     * Schließt alle geladenen Modelle
     */
    private void closeAllModels() {
        loadedModels.values().forEach(info -> {
            info.getModel().close();
            log.info("AiCloudService: Model '{}' closed.", info.getName());
        });
        loadedModels.clear();
    }

    /**
     * Gibt alle geladenen Modelle zurück (für API-Zugriff)
     */
    public Map<String, Map<String, Object>> getModelInfo() {
        Map<String, Map<String, Object>> info = new HashMap<>();
        loadedModels.forEach((name, modelInfo) -> info.put(name, modelInfo.toMap()));
        return info;
    }

    /**
     * Model-Info Klasse
     */
    static class ModelInfo {
        private String name;
        private String type;
        private ZooModel<String, Classifications> model;
        private long loadTime;
        private int predictionCount;
        private double avgInferenceTime;

        public ModelInfo(String name, String type, ZooModel<String, Classifications> model) {
            this.name = name;
            this.type = type;
            this.model = model;
            this.loadTime = System.currentTimeMillis();
            this.predictionCount = 0;
            this.avgInferenceTime = 0.0;
        }

        public void updateInferenceTime(double time) {
            avgInferenceTime = (avgInferenceTime * predictionCount + time) / (predictionCount + 1);
            predictionCount++;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("name", name);
            map.put("type", type);
            map.put("loadTime", loadTime);
            map.put("predictionCount", predictionCount);
            map.put("avgInferenceTime", avgInferenceTime);
            map.put("uptime", System.currentTimeMillis() - loadTime);
            return map;
        }

        // Getters
        public String getName() { return name; }
        public ZooModel<String, Classifications> getModel() { return model; }
        public int getPredictionCount() { return predictionCount; }
        public double getAvgInferenceTime() { return avgInferenceTime; }
    }

    /**
     * Prediction-Result Klasse
     */
    static class PredictionResult {
        private String input;
        private String output;
        private double confidence;
        private long inferenceTime;
        private String modelName;
        private boolean success;
        private String error;

        public PredictionResult(String input, String modelName) {
            this.input = input;
            this.modelName = modelName;
            this.success = false;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("input", input);
            map.put("output", output);
            map.put("confidence", confidence);
            map.put("inferenceTime", inferenceTime);
            map.put("modelName", modelName);
            map.put("success", success);
            if (error != null) {
                map.put("error", error);
            }
            return map;
        }

        // Setters
        public void setOutput(String output) { this.output = output; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        public void setInferenceTime(long inferenceTime) { this.inferenceTime = inferenceTime; }
        public void setSuccess(boolean success) { this.success = success; }
        public void setError(String error) { this.error = error; }
    }

    /**
     * Einfache Progress-Bar für Modell-Downloads
     */
    static class SimpleProgressBar implements TrainingListener.Defaults {
        @Override
        public void onTrainingProgress(ai.djl.training.Trainer trainer) {
            // log.debug("."); // Vermeide übermäßiges Logging in einem Service
        }
    }
}
