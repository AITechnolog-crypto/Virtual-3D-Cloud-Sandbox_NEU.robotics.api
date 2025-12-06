package com.june.production;

import java.util.List;

/**
 * ML-Modell für Ressourcenoptimierung (lokal im production-Package).
 */
public interface IMachineLearningModel {
    void train(List<ResourceData> data);
    ResourcePrediction predict(ResourceData newData);
}