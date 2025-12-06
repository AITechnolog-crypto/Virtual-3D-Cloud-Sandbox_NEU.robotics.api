package com.june.spaceai;

import java.util.List;
import java.util.logging.Logger;

/**
 * Beispielimplementierung eines ML-Modells: trainiert und sagt einen einfachen Summenwert vorher.
 */
public class ExampleMLModel implements IMachineLearningModel {
    private static final Logger logger = Logger.getLogger(ExampleMLModel.class.getName());

    @Override
    public void train(List<CombinedData> trainingData) {
        logger.info("ML-Modell trainiert mit: " + trainingData);
    }

    @Override
    public CombinedPrediction predict(CombinedData currentData) {
        logger.info("Vorhersage für: " + currentData);
        // Beispielhafte Vorhersage: Summe aus Energielevel und Signalstärke
        return new CombinedPrediction(currentData.energyLevel + currentData.signalStrength);
    }
}
