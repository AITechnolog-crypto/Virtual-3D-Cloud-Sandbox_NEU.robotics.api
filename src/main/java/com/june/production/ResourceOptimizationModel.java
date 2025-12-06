package com.june.production;

import java.util.List;
import java.util.logging.Logger;

/**
 * Dummy-Optimierungsmodell: trainiert auf Ressourcendaten und gibt eine einfache Vorhersage zurück.
 */
public class ResourceOptimizationModel implements IMachineLearningModel {
    private static final Logger logger = Logger.getLogger(ResourceOptimizationModel.class.getName());

    private boolean trained;
    private double avgAmount;

    @Override
    public void train(List<ResourceData> data) {
        if (data == null || data.isEmpty()) {
            logger.info("Keine Trainingsdaten vorhanden.");
            trained = false;
            avgAmount = 0.0;
            return;
        }
        double sum = 0.0; int n = 0;
        for (ResourceData d : data) { if (d != null) { sum += d.getAmount(); n++; } }
        if (n > 0) { avgAmount = sum / n; trained = true; }
        logger.info("Training abgeschlossen. Durchschnittsmenge=" + avgAmount);
    }

    @Override
    public ResourcePrediction predict(ResourceData newData) {
        if (!trained) {
            return new ResourcePrediction(0.0, 0.1);
        }
        double base = newData != null ? newData.getAmount() : avgAmount;
        return new ResourcePrediction(base * 0.9 + avgAmount * 0.1, 0.7);
    }
}