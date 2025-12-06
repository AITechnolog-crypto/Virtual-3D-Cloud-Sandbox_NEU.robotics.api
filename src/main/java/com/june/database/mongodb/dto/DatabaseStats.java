package com.june.database.mongodb.dto;

import lombok.Data;

@Data
public class DatabaseStats {
    public final Long sessionCount;
    public final Long authCount;
    public final Double avgSolarEnergy;

    public DatabaseStats(Long sessionCount, Long authCount, Double avgSolarEnergy) {
        this.sessionCount = sessionCount;
        this.authCount = authCount;
        this.avgSolarEnergy = avgSolarEnergy;
    }
}
