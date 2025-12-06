package com.june.ml.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import weka.classifiers.trees.J48;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.InputStream;

/**
 * 🎓 Weka Learning Machine Example Service
 *
 * Demonstriert vollständigen Weka-Workflow:
 * - Laden von ARFF-Datensätzen (Iris, etc.)
 * - Training von J48 Decision Tree
 * - Klassifizierung neuer Datenpunkte
 *
 * @author Sanel Crnkic - NurSystem Pro
 * @version 1.0
 */
@Service
@ConditionalOnClass(J48.class)
public class WekaExampleService {

    @Value("classpath:datasets/iris.arff")
    private Resource irisDataset;

    private J48 trainedClassifier;
    private Instances trainingData;

    /**
     * Trainiert einen J48 Classifier mit Iris-Datensatz
     */
    public String trainIrisClassifier() {
        try {
            System.out.println("🎓 WekaExampleService: Training J48 Classifier mit Iris-Datensatz");

            // Lade Trainingsdaten
            DataSource source;
            if (irisDataset.exists()) {
                InputStream is = irisDataset.getInputStream();
                source = new DataSource(is);
            } else {
                // Fallback: Generiere synthetische Iris-Daten
                System.out.println("⚠️ iris.arff nicht gefunden - nutze synthetische Daten");
                trainingData = generateSyntheticIrisData();
                source = null;
            }

            if (source != null) {
                trainingData = source.getDataSet();
            }

            // Setze Class-Index (letztes Attribut)
            if (trainingData.classIndex() == -1) {
                trainingData.setClassIndex(trainingData.numAttributes() - 1);
            }

            // Initialisiere und trainiere J48
            trainedClassifier = new J48();
            trainedClassifier.buildClassifier(trainingData);

            // Ausgabe der Entscheidungsregeln
            String tree = trainedClassifier.toString();
            System.out.println("✅ Training abgeschlossen!");
            System.out.println("Entscheidungsbaum:\n" + tree);

            return "J48 Classifier erfolgreich trainiert. Datenpunkte: " + trainingData.numInstances();

        } catch (Exception e) {
            e.printStackTrace();
            return "Fehler beim Training: " + e.getMessage();
        }
    }

    /**
     * Klassifiziert einen neuen Datenpunkt (Iris-Features)
     */
    public String classifyIrisInstance(double sepalLength, double sepalWidth, double petalLength, double petalWidth) {
        try {
            if (trainedClassifier == null || trainingData == null) {
                return "Classifier nicht trainiert. Bitte zuerst trainIrisClassifier() aufrufen.";
            }

            // Erstelle neuen Datenpunkt
            double[] values = new double[trainingData.numAttributes()];
            values[0] = sepalLength;
            values[1] = sepalWidth;
            values[2] = petalLength;
            values[3] = petalWidth;
            // Class-Wert (wird vom Classifier gesetzt)
            values[4] = 0;

            DenseInstance instance = new DenseInstance(1.0, values);
            instance.setDataset(trainingData);

            // Klassifiziere
            double prediction = trainedClassifier.classifyInstance(instance);
            String predictedClass = trainingData.classAttribute().value((int) prediction);

            // Confidence
            double[] distribution = trainedClassifier.distributionForInstance(instance);
            double confidence = distribution[(int) prediction];

            System.out.println("🎯 Vorhersage: " + predictedClass + " (Confidence: " + String.format("%.2f%%", confidence * 100) + ")");

            return String.format("Predicted Class: %s (Confidence: %.2f%%)", predictedClass, confidence * 100);

        } catch (Exception e) {
            e.printStackTrace();
            return "Fehler bei Klassifizierung: " + e.getMessage();
        }
    }

    /**
     * Gibt den trainierten Entscheidungsbaum als String zurück
     */
    public String getDecisionTree() {
        if (trainedClassifier == null) {
            return "Kein Classifier trainiert.";
        }
        return trainedClassifier.toString();
    }

    /**
     * Generiert synthetische Iris-Daten falls ARFF nicht verfügbar
     */
    private Instances generateSyntheticIrisData() {
        // Simplified Iris dataset generation
        String arffHeader = "@relation iris\n\n" +
            "@attribute sepallength numeric\n" +
            "@attribute sepalwidth numeric\n" +
            "@attribute petallength numeric\n" +
            "@attribute petalwidth numeric\n" +
            "@attribute class {Iris-setosa,Iris-versicolor,Iris-virginica}\n\n" +
            "@data\n" +
            "5.1,3.5,1.4,0.2,Iris-setosa\n" +
            "4.9,3.0,1.4,0.2,Iris-setosa\n" +
            "4.7,3.2,1.3,0.2,Iris-setosa\n" +
            "7.0,3.2,4.7,1.4,Iris-versicolor\n" +
            "6.4,3.2,4.5,1.5,Iris-versicolor\n" +
            "6.9,3.1,4.9,1.5,Iris-versicolor\n" +
            "6.3,3.3,6.0,2.5,Iris-virginica\n" +
            "5.8,2.7,5.1,1.9,Iris-virginica\n" +
            "7.1,3.0,5.9,2.1,Iris-virginica\n";

        try {
            java.io.StringReader reader = new java.io.StringReader(arffHeader);
            Instances data = new Instances(reader);
            data.setClassIndex(data.numAttributes() - 1);
            return data;
        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Generieren synthetischer Daten", e);
        }
    }
}
