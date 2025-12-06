package com.june.ai.notes;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * MongoDB Document für Umweltdaten
 */
@Document(collection = "#{@notesAiConfig.mongo.environmentalDataCollection}")
public class EnvironmentalDataDocument {

    @Id
    private String id;

    private String dataId;
    private String location;
    private String region;

    // Core Environmental Data
    private boolean protectedArea;
    private boolean willAffectBiodiversity;
    private double soilQualityIndex;
    private boolean hasWaterSources;
    private double vegetationDensity;

    // Extended Environmental Data
    private double airQualityIndex;
    private double noiseLevel;
    private double temperatureAvg;
    private double humidityLevel;
    private double precipitationLevel;

    // Coordinates
    private double latitude;
    private double longitude;
    private double elevation;

    // Assessment Results
    private EnvironmentalAssessment assessment;

    // Metadata
    private String dataSource;
    private LocalDateTime measuredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public EnvironmentalDataDocument() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.measuredAt = LocalDateTime.now();
    }

    public EnvironmentalDataDocument(boolean protectedArea, boolean willAffectBiodiversity,
                                   double soilQualityIndex, boolean hasWaterSources,
                                   double vegetationDensity) {
        this();
        this.protectedArea = protectedArea;
        this.willAffectBiodiversity = willAffectBiodiversity;
        this.soilQualityIndex = soilQualityIndex;
        this.hasWaterSources = hasWaterSources;
        this.vegetationDensity = vegetationDensity;

        // Automatische Bewertung durchführen
        this.assessment = performAssessment();
    }

    /**
     * Bewertung der Bodenqualität
     */
    public boolean isSoilQualityAcceptable() {
        return soilQualityIndex > 50.0; // Schwellenwert: 50
    }

    /**
     * Bewertung der Wasserquellen-Verfügbarkeit
     */
    public boolean areWaterSourcesSufficient() {
        return hasWaterSources;
    }

    /**
     * Bewertung der Vegetationsdichte
     */
    public boolean isVegetationDenseEnough() {
        return vegetationDensity > 70.0; // Schwellenwert: 70%
    }

    /**
     * Bewertung der Luftqualität
     */
    public boolean isAirQualityAcceptable() {
        return airQualityIndex < 100.0; // AQI < 100 = akzeptabel
    }

    /**
     * Gesamtbewertung der Umweltverträglichkeit
     */
    public boolean isEnvironmentallyCompatible() {
        return !protectedArea &&
               !willAffectBiodiversity &&
               isSoilQualityAcceptable() &&
               areWaterSourcesSufficient() &&
               isVegetationDenseEnough() &&
               isAirQualityAcceptable();
    }

    /**
     * Berechnet Umwelt-Score (0-100)
     */
    public double calculateEnvironmentalScore() {
        double score = 0.0;
        int factors = 0;

        // Bodenqualität (20%)
        score += (soilQualityIndex / 100.0) * 20.0;
        factors++;

        // Vegetationsdichte (20%)
        score += (vegetationDensity / 100.0) * 20.0;
        factors++;

        // Luftqualität (15%) - invertiert (niedrigere AQI = besser)
        if (airQualityIndex > 0) {
            score += (1.0 - Math.min(airQualityIndex / 200.0, 1.0)) * 15.0;
            factors++;
        }

        // Wasserquellen (15%)
        if (hasWaterSources) score += 15.0;
        factors++;

        // Schutzgebiet (15%) - negativ
        if (!protectedArea) score += 15.0;
        factors++;

        // Biodiversitäts-Impact (15%) - negativ
        if (!willAffectBiodiversity) score += 15.0;
        factors++;

        return Math.max(0.0, Math.min(100.0, score));
    }

    /**
     * Führt vollständige Umweltbewertung durch
     */
    private EnvironmentalAssessment performAssessment() {
        EnvironmentalAssessment result = new EnvironmentalAssessment();

        result.setOverallScore(calculateEnvironmentalScore());
        result.setCompatible(isEnvironmentallyCompatible());
        result.setSoilQualityAcceptable(isSoilQualityAcceptable());
        result.setWaterSourcesSufficient(areWaterSourcesSufficient());
        result.setVegetationDenseEnough(isVegetationDenseEnough());
        result.setAirQualityAcceptable(isAirQualityAcceptable());

        // Risiko-Level bestimmen
        double score = result.getOverallScore();
        if (score >= 80.0) {
            result.setRiskLevel("LOW");
        } else if (score >= 60.0) {
            result.setRiskLevel("MEDIUM");
        } else {
            result.setRiskLevel("HIGH");
        }

        // Empfehlungen generieren
        result.setRecommendations(generateRecommendations());

        return result;
    }

    /**
     * Generiert Empfehlungen basierend auf Umweltdaten
     */
    private Map<String, String> generateRecommendations() {
        Map<String, String> recommendations = new HashMap<>();

        if (!isSoilQualityAcceptable()) {
            recommendations.put("soil", "Bodenverbesserung erforderlich - Kompostierung oder Bodenaufbereitung empfohlen");
        }

        if (!areWaterSourcesSufficient()) {
            recommendations.put("water", "Wasserversorgung sicherstellen - Brunnen oder Wasserspeicher installieren");
        }

        if (!isVegetationDenseEnough()) {
            recommendations.put("vegetation", "Aufforstung oder Begrünung empfohlen - einheimische Pflanzen bevorzugen");
        }

        if (!isAirQualityAcceptable()) {
            recommendations.put("air", "Luftqualität verbessern - Emissionen reduzieren, Luftfilter installieren");
        }

        if (protectedArea) {
            recommendations.put("protection", "Schutzgebiet - spezielle Genehmigungen und Auflagen erforderlich");
        }

        if (willAffectBiodiversity) {
            recommendations.put("biodiversity", "Biodiversitäts-Impact minimieren - Ausgleichsmaßnahmen erforderlich");
        }

        return recommendations;
    }

    public static class EnvironmentalAssessment {
        private double overallScore;
        private boolean compatible;
        private String riskLevel;

        private boolean soilQualityAcceptable;
        private boolean waterSourcesSufficient;
        private boolean vegetationDenseEnough;
        private boolean airQualityAcceptable;

        private Map<String, String> recommendations;
        private LocalDateTime assessedAt;

        public EnvironmentalAssessment() {
            this.assessedAt = LocalDateTime.now();
            this.recommendations = new HashMap<>();
        }

        // Getters & Setters
        public double getOverallScore() { return overallScore; }
        public void setOverallScore(double overallScore) { this.overallScore = overallScore; }

        public boolean isCompatible() { return compatible; }
        public void setCompatible(boolean compatible) { this.compatible = compatible; }

        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

        public boolean isSoilQualityAcceptable() { return soilQualityAcceptable; }
        public void setSoilQualityAcceptable(boolean soilQualityAcceptable) { this.soilQualityAcceptable = soilQualityAcceptable; }

        public boolean isWaterSourcesSufficient() { return waterSourcesSufficient; }
        public void setWaterSourcesSufficient(boolean waterSourcesSufficient) { this.waterSourcesSufficient = waterSourcesSufficient; }

        public boolean isVegetationDenseEnough() { return vegetationDenseEnough; }
        public void setVegetationDenseEnough(boolean vegetationDenseEnough) { this.vegetationDenseEnough = vegetationDenseEnough; }

        public boolean isAirQualityAcceptable() { return airQualityAcceptable; }
        public void setAirQualityAcceptable(boolean airQualityAcceptable) { this.airQualityAcceptable = airQualityAcceptable; }

        public Map<String, String> getRecommendations() { return recommendations; }
        public void setRecommendations(Map<String, String> recommendations) { this.recommendations = recommendations; }

        public LocalDateTime getAssessedAt() { return assessedAt; }
        public void setAssessedAt(LocalDateTime assessedAt) { this.assessedAt = assessedAt; }
    }

    // Main Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDataId() { return dataId; }
    public void setDataId(String dataId) { this.dataId = dataId; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public boolean isProtectedArea() { return protectedArea; }
    public void setProtectedArea(boolean protectedArea) { this.protectedArea = protectedArea; }

    public boolean isWillAffectBiodiversity() { return willAffectBiodiversity; }
    public void setWillAffectBiodiversity(boolean willAffectBiodiversity) { this.willAffectBiodiversity = willAffectBiodiversity; }

    public double getSoilQualityIndex() { return soilQualityIndex; }
    public void setSoilQualityIndex(double soilQualityIndex) { this.soilQualityIndex = soilQualityIndex; }

    public boolean isHasWaterSources() { return hasWaterSources; }
    public void setHasWaterSources(boolean hasWaterSources) { this.hasWaterSources = hasWaterSources; }

    public double getVegetationDensity() { return vegetationDensity; }
    public void setVegetationDensity(double vegetationDensity) { this.vegetationDensity = vegetationDensity; }

    public double getAirQualityIndex() { return airQualityIndex; }
    public void setAirQualityIndex(double airQualityIndex) { this.airQualityIndex = airQualityIndex; }

    public double getNoiseLevel() { return noiseLevel; }
    public void setNoiseLevel(double noiseLevel) { this.noiseLevel = noiseLevel; }

    public double getTemperatureAvg() { return temperatureAvg; }
    public void setTemperatureAvg(double temperatureAvg) { this.temperatureAvg = temperatureAvg; }

    public double getHumidityLevel() { return humidityLevel; }
    public void setHumidityLevel(double humidityLevel) { this.humidityLevel = humidityLevel; }

    public double getPrecipitationLevel() { return precipitationLevel; }
    public void setPrecipitationLevel(double precipitationLevel) { this.precipitationLevel = precipitationLevel; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double getElevation() { return elevation; }
    public void setElevation(double elevation) { this.elevation = elevation; }

    public EnvironmentalAssessment getAssessment() { return assessment; }
    public void setAssessment(EnvironmentalAssessment assessment) { this.assessment = assessment; }

    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }

    public LocalDateTime getMeasuredAt() { return measuredAt; }
    public void setMeasuredAt(LocalDateTime measuredAt) { this.measuredAt = measuredAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
