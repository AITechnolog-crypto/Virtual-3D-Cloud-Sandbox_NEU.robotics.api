package com.june.ml.model;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EnergyPredictionModel {
    public long trainedTs;
    public long n;
    public double slope;
    public double intercept;
    public double min, max, avg;

    public Map<String,Object> toMap(){
        return new LinkedHashMap<>(Map.of(
                "trained", true,
                "trainedTs", trainedTs,
                "n", n,
                "slope", slope,
                "intercept", intercept,
                "min", min,
                "max", max,
                "avg", avg
        ));
    }
    public double predictNext(int step){
        double x = (n + step);
        return intercept + slope * x;
    }
}
