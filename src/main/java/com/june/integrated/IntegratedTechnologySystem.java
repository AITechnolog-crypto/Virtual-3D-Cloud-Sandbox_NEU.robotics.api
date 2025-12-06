package com.june.integrated;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Integriertes Technologiesystem: orchestriert alte und moderne Technologie
 * und nutzt ein ML-Modell, um basierend auf Sensordaten Entscheidungen zu treffen.
 */
public class IntegratedTechnologySystem {
    private static final Logger logger = Logger.getLogger(IntegratedTechnologySystem.class.getName());

    private final IAncientTechnology ancientTech;
    private final IModernTechnology modernTech;
    private final IMachineLearningModel predictionModel;

    public IntegratedTechnologySystem(IAncientTechnology ancientTech,
                                      IModernTechnology modernTech,
                                      IMachineLearningModel predictionModel) {
        this.ancientTech = Objects.requireNonNull(ancientTech, "ancientTech");
        this.modernTech = Objects.requireNonNull(modernTech, "modernTech");
        this.predictionModel = Objects.requireNonNull(predictionModel, "predictionModel");
    }

    public void integrateAndOperate() {
        try {
            ancientTech.performAncientTasks();
            modernTech.performModernTasks();

            List<SensorData> historicalData = getHistoricalSensorData();
            predictionModel.train(historicalData);
            SensorData currentData = getCurrentSensorData();
            Prediction prediction = predictionModel.makePrediction(currentData);

            executeBasedOnPrediction(prediction);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Fehler im Systembetrieb: ", e);
        }
    }

    private List<SensorData> getHistoricalSensorData() {
        List<SensorData> data = new ArrayList<>();
        data.add(new SensorData(20, 1000));
        data.add(new SensorData(22, 1010));
        return data;
    }

    private SensorData getCurrentSensorData() {
        return new SensorData(21, 1005);
    }

    private void executeBasedOnPrediction(Prediction prediction) {
        logger.info("Ausführung von Aktionen basierend auf der Vorhersage: " + prediction);
        if (prediction != null && prediction.getPredictedValue() > 25) {
            logger.info("Warnung: Hoher Vorhersagewert!");
        }
    }
}
