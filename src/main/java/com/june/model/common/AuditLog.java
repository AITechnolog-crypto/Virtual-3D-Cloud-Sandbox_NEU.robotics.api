package com.june.model.common;

import java.time.Instant;

public class AuditLog {
    private Instant timestamp = Instant.now();
    private String actor;
    private String action;
    private String details;

    public AuditLog() {}
    public AuditLog(String actor, String action, String details){ this.actor=actor; this.action=action; this.details=details; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}