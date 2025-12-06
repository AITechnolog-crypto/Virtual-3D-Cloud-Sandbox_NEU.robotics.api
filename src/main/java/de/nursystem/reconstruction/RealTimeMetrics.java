package de.nursystem.reconstruction;

/**
 * Echtzeit-Metriken
 */
class RealTimeMetrics {
    private final String droneId;
    private final double efficiency;
    private final int remainingTime;
    private final double quality;

    public RealTimeMetrics(String droneId, double efficiency, int remainingTime, double quality) {
        this.droneId = droneId;
        this.efficiency = efficiency;
        this.remainingTime = remainingTime;
        this.quality = quality;
    }

    public String getDroneId() { return droneId; }
    public double getEfficiency() { return efficiency; }
    public int getRemainingTime() { return remainingTime; }
    public double getQuality() { return quality; }
}
