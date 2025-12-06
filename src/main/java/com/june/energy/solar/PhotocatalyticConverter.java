package com.june.energy.solar;

/**
 * Einfacher, eigenständiger Photokatalysator ohne Lombok/Spring-Abhängigkeiten.
 */
public class PhotocatalyticConverter {
    private String converterId = "CONV-" + System.currentTimeMillis();
    private double conversionRate = 0.75; // 75%
    private double totalCO2Converted = 0.0;
    private double totalChemicalsProduced = 0.0;

    public double convertCO2(double co2Amount) {
        double converted = co2Amount * conversionRate;
        totalCO2Converted += converted;
        totalChemicalsProduced += converted * 0.5; // Simuliert
        System.out.println("[PhotocatalyticConverter] CO2 konvertiert: " + co2Amount + " kg -> " + (converted * 0.5) + " kg Chemikalien");
        return converted * 0.5;
    }

    public void optimizeConversion() {
        double oldRate = this.conversionRate;
        this.conversionRate = Math.min(0.9, this.conversionRate * 1.05);
        System.out.println("[PhotocatalyticConverter] Konverter " + converterId + " Effizienz optimiert: " + (oldRate * 100) + "% -> " + (conversionRate * 100) + "%");
    }

    public double getCO2Reduction() {
        return totalCO2Converted;
    }

    public String getConverterId() { return converterId; }
    public void setConverterId(String converterId) { this.converterId = converterId; }
    public double getConversionRate() { return conversionRate; }
    public void setConversionRate(double conversionRate) { this.conversionRate = conversionRate; }
    public double getTotalCO2Converted() { return totalCO2Converted; }
    public double getTotalChemicalsProduced() { return totalChemicalsProduced; }
}
