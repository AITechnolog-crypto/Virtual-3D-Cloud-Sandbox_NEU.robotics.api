package de.nursystem.visionaid;

/**
 * Satelliten-Status
 */
class SatellitenStatus {
    private final String satellit;
    private final boolean verbunden;
    private final double signalstaerke;
    private final String status;

    public SatellitenStatus(String satellit, boolean verbunden, double signalstaerke, String status) {
        this.satellit = satellit;
        this.verbunden = verbunden;
        this.signalstaerke = signalstaerke;
        this.status = status;
    }

    public String getSatellit() { return satellit; }
    public boolean isVerbunden() { return verbunden; }
    public double getSignalstaerke() { return signalstaerke; }
    public String getStatus() { return status; }
}
