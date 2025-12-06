package com.june.spaceai;

import java.util.List;

/**
 * Scoped ML-Interface für das SpaceAI-Modul (vermeidet Namenskonflikte).
 */
public interface IMachineLearningModel {
    void train(List<CombinedData> trainingData);
    CombinedPrediction predict(CombinedData currentData);
}
