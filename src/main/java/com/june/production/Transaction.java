package com.june.production;

/**
 * Transaktion, die eine ResourcePrediction als Kontext enthält.
 */
public class Transaction {
    private ResourcePrediction prediction;

    public Transaction(ResourcePrediction prediction) {
        this.prediction = prediction;
    }

    public ResourcePrediction getPrediction() {
        return prediction;
    }

    public void setPrediction(ResourcePrediction prediction) {
        this.prediction = prediction;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "prediction=" + prediction +
                '}';
    }
}