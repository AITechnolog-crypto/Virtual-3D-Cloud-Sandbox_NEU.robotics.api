package com.june.integrated;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Beispiel-ML-Modell gemäß Anforderung: trainiert auf Historie und sagt auf Basis aktueller Daten voraus.
 */
public class MachineLearningModel implements IMachineLearningModel {
    private static final Logger logger = Logger.getLogger(MachineLearningModel.class.getName());

    @Override
    public void train(List<SensorData> trainingData) {
        logger.log(Level.INFO, "Training des maschinellen Lernmodells mit Sensordaten. Größe: {0}",
                trainingData != null ? trainingData.size() : 0);
        // Hier wäre echte Trainingslogik.
    }

    @Override
    public Prediction makePrediction(SensorData currentData) {
        logger.log(Level.INFO, "Erstellung einer Vorhersage basierend auf aktuellen Sensordaten: {0}", currentData);
        // Beispielhafte Vorhersage (Dummy):
        double base = currentData != null ? (currentData.getTemperature() * 0.8 + currentData.getPressure() * 0.01) : 0.0;
        double predicted = Math.round(base * 100.0) / 100.0;
        return new Prediction(predicted, 0.8);
    }
}
