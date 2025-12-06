package com.june.integrated;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dummy-ML-Modell: Trainiert einmal auf historischen Sensordaten und
 * sagt als Vorhersage den Durchschnitt der Temperatur voraus.
 * Entspricht der Spezifikation im Issue-Text.
 */
public class AverageTemperatureModel implements IMachineLearningModel {
    private static final Logger logger = Logger.getLogger(AverageTemperatureModel.class.getName());

    private boolean isTrained = false;
    private double averageValue = 0.0;

    @Override
    public void train(List<SensorData> trainingData) {
        if (isTrained) {
            logger.info("Modell ist bereits trainiert.");
            return;
        }
        if (trainingData == null || trainingData.isEmpty()) {
            logger.info("Keine Trainingsdaten vorhanden.");
            return;
        }
        logger.info("Training des maschinellen Lernmodells mit Sensordaten.");
        double sum = 0.0;
        int n = 0;
        for (SensorData d : trainingData) {
            if (d == null) continue;
            sum += d.getTemperature();
            n++;
        }
        if (n > 0) {
            averageValue = sum / n;
            isTrained = true;
            logger.log(Level.FINE, "Durchschnitt berechnet: {0}", averageValue);
        } else {
            logger.info("Keine gültigen Trainingsdatensätze.");
        }
    }

    @Override
    public Prediction makePrediction(SensorData currentData) {
        logger.info("Erstellung einer Vorhersage basierend auf aktuellen Sensordaten: " + currentData);
        if (!isTrained) {
            logger.warning("Modell ist nicht trainiert. Es wird ein Standardwert zurückgegeben.");
            return new Prediction(0.0, 0.1);
        }
        // Dummy: gebe den gelernten Durchschnittswert zurück, Confidence moderat
        return new Prediction(averageValue, 0.6);
    }
}
