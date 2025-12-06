package com.june.spaceai;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Kombinierte KI: sammelt Energie- und Satellitendaten, kombiniert sie,
 * trainiert ein ML-Modell und wendet eine Vorhersage an.
 */
public class CombinedArtificialIntelligence implements IArtificialIntelligence {
    private final Logger logger = Logger.getLogger(CombinedArtificialIntelligence.class.getName());
    private final IMachineLearningModel mlModel;
    private final ISpaceEnergyHarvester energyHarvester;
    private final ISatelliteEnhancer satelliteEnhancer;

    public CombinedArtificialIntelligence(IMachineLearningModel mlModel,
                                          ISpaceEnergyHarvester energyHarvester,
                                          ISatelliteEnhancer satelliteEnhancer) {
        this.mlModel = mlModel;
        this.energyHarvester = energyHarvester;
        this.satelliteEnhancer = satelliteEnhancer;
    }

    @Override
    public void implementLogic() {
        try {
            List<CombinedData> combinedData = collectAndCombineData();
            trainModel(combinedData);
            CombinedPrediction prediction = makePrediction();
            applyPrediction(prediction);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Fehler in der KI-Logik", e);
        }
    }

    private List<CombinedData> collectAndCombineData() {
        List<ResourceData> energyData = energyHarvester.collectEnergyData();
        List<SatelliteData> satelliteData = satelliteEnhancer.collectSatelliteData();
        return combineData(energyData, satelliteData);
    }

    private List<CombinedData> combineData(List<ResourceData> energyData, List<SatelliteData> satelliteData) {
        List<CombinedData> combinedData = new ArrayList<>();
        for (ResourceData energy : energyData) {
            for (SatelliteData satellite : satelliteData) {
                combinedData.add(new CombinedData(energy.energyLevel, satellite.signalStrength));
            }
        }
        return combinedData;
    }

    private void trainModel(List<CombinedData> combinedData) {
        mlModel.train(combinedData);
    }

    private CombinedPrediction makePrediction() {
        return mlModel.predict(new CombinedData(80, 92)); // Beispielhafte Eingabedaten
    }

    private void applyPrediction(CombinedPrediction prediction) {
        logger.info("KI-Vorhersage angewendet: " + prediction);
    }

    public static void main(String[] args) {
        IMachineLearningModel mlModel = new ExampleMLModel();
        ISpaceEnergyHarvester energyHarvester = new ExampleEnergyHarvester();
        ISatelliteEnhancer satelliteEnhancer = new ExampleSatelliteEnhancer();
        CombinedArtificialIntelligence ai = new CombinedArtificialIntelligence(mlModel, energyHarvester, satelliteEnhancer);
        ai.implementLogic();
    }
}
