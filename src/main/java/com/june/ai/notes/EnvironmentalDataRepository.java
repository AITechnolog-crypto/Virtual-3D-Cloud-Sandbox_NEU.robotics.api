package com.june.ai.notes;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB Repository für Umweltdaten
 */
@Repository
@ConditionalOnProperty(prefix = "ai.notes", name = "enabled", havingValue = "true")
public interface EnvironmentalDataRepository extends MongoRepository<EnvironmentalDataDocument, String> {

    Optional<EnvironmentalDataDocument> findByDataId(String dataId);

    List<EnvironmentalDataDocument> findByLocation(String location);

    List<EnvironmentalDataDocument> findByRegion(String region);

    List<EnvironmentalDataDocument> findByProtectedArea(boolean protectedArea);

    List<EnvironmentalDataDocument> findBySoilQualityIndexGreaterThan(double soilQualityIndex);

    List<EnvironmentalDataDocument> findByVegetationDensityGreaterThan(double vegetationDensity);

    List<EnvironmentalDataDocument> findByHasWaterSources(boolean hasWaterSources);

    List<EnvironmentalDataDocument> findByMeasuredAtAfter(LocalDateTime date);

    List<EnvironmentalDataDocument> findByDataSource(String dataSource);

    // Geo-Spatial Queries (vereinfacht)
    List<EnvironmentalDataDocument> findByLatitudeBetweenAndLongitudeBetween(
        double latMin, double latMax, double lonMin, double lonMax);

    // Assessment Queries
    List<EnvironmentalDataDocument> findByAssessment_Compatible(boolean compatible);

    List<EnvironmentalDataDocument> findByAssessment_RiskLevel(String riskLevel);

    List<EnvironmentalDataDocument> findByAssessment_OverallScoreGreaterThan(double score);

    long countByProtectedArea(boolean protectedArea);

    long countByAssessment_Compatible(boolean compatible);
}
