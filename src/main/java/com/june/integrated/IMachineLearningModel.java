package com.june.integrated;

import java.util.List;

/**
 * Abstraktion für ML-Modell: Training und Vorhersage.
 */
public interface IMachineLearningModel {
    void train(List<SensorData> trainingData);
    Prediction makePrediction(SensorData currentData);
}
