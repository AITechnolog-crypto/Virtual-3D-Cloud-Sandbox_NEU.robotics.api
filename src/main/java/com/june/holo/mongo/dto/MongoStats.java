package com.june.holo.mongo.dto;

import lombok.Data;

@Data
public class MongoStats {
    public final Long sessionCount;
    public final Integer windowCount;
    public final Double avgSolarEnergy;

    public MongoStats(Long sessionCount, Integer windowCount, Double avgSolarEnergy) {
        this.sessionCount = sessionCount;
        this.windowCount = windowCount;
        this.avgSolarEnergy = avgSolarEnergy;
    }
}
