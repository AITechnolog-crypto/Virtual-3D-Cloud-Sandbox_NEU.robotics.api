package com.june.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Platzhalter für SchutzAnzug
 */
@Data
@Builder
public class SchutzAnzug {
    private String id;
    private String name;
    private String modell;
    private String serialNummer;
    private String status;
    private int schutzLevel;
    private boolean transformationsFaehigkeit;
    private boolean fuchsformModus;
    private double energieLevel;
    private String biometrischerBesitzer;
    private String lagerort;
    private LocalDateTime letzteWartung;
    private LocalDateTime naechsteWartung;
}
