package com.june.integrated;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Autonomes System mit Sensordatenintegration.
 * Trainiert beim ersten Lauf das ML‑Modell auf historischen Daten und
 * führt anschließend Aktionen basierend auf der Vorhersage aus.
 */
public class AutonomousSystemWithSensorData {
    private static final Logger logger = Logger.getLogger(AutonomousSystemWithSensorData.class.getName());

    private final IMachineLearningModel predictionModel;
    private boolean isFirstRun = true;

    public AutonomousSystemWithSensorData(IMachineLearningModel predictionModel) {
        this.predictionModel = predictionModel;
    }

    public void operateSystem(SensorData currentData) {
        try {
            if (isFirstRun) {
                predictionModel.train(getHistoricalSensorData());
                isFirstRun = false;
            }
            Prediction prediction = predictionModel.makePrediction(currentData);
            executeBasedOnPrediction(prediction);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Fehler im autonomen System: ", e);
        }
    }

    private List<SensorData> getHistoricalSensorData() {
        List<SensorData> historicalData = new ArrayList<>();
        historicalData.add(new SensorData(20, 1000));
        historicalData.add(new SensorData(22, 1010));
        historicalData.add(new SensorData(18, 990));
        return historicalData;
    }

    private void executeBasedOnPrediction(Prediction prediction) {
        logger.info("Ausführung von Aktionen basierend auf der Vorhersage: " + prediction);
        // Beispielhafte Logik: branch nach Schwellenwert
        if (prediction != null && prediction.getPredictedValue() > 25) {
            logger.info("Warnung: Hoher Vorhersage wert!");
        }
    }

    public static void main(String[] args) {
        IMachineLearningModel model = new AverageTemperatureModel();
        AutonomousSystemWithSensorData system = new AutonomousSystemWithSensorData(model);

        system.operateSystem(new SensorData(25, 1020));
        system.operateSystem(new SensorData(26, 1030));
    }
}
