package com.june.de.nursystem.reconstruction;

/**
 * Sicherheitsvorfall
 */
public class SafetyIncident {
    private final String droneId;
    private final String description;
    private final IncidentSeverity severity;
    private final long timestamp;

    public SafetyIncident(String droneId, String description, IncidentSeverity severity) {
        this.droneId = droneId;
        this.description = description;
        this.severity = severity;
        this.timestamp = System.currentTimeMillis();
    }

    public String getDroneId() { return droneId; }
    public String getDescription() { return description; }
    public IncidentSeverity getSeverity() { return severity; }
    public long getTimestamp() { return timestamp; }
}
