package com.june.geospatial.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Repräsentiert eine zwischengespeicherte Analyse eines Ortes (einer "Welt-Kachel").
 * Dient als Wissensdatenbank für Agenten.
 */
@Data
@Document(collection = "place_cache")
@NoArgsConstructor
public class PlaceCache {

    @Id
    private String id;

    // Basis-Informationen
    private String name; // Name des Ortes/der Kachel
    private double lat;
    private double lng;

    // Analyse-Ergebnisse
    private String concept; // Das extrahierte Kernthema (z.B. "Solarfarm", "Logistikzentrum")
    private String discipline; // Übergeordnete Disziplin (z.B. "Energie", "Wirtschaft")
    private String summary; // KI-generierte Zusammenfassung der Analyse
    private double relevanceScore; // KI-bewertete Relevanz (0.0 - 1.0)

    // Metadaten für Robotik/Physik
    private String additionalData; // JSON-String für flexible Zusatzdaten (z.B. Steigung, Befahrbarkeit)

    // Zeitstempel
    private Instant createdAt;

    public PlaceCache(String name, double lat, double lng, String concept, String discipline, String summary, double relevanceScore, String additionalData) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.concept = concept;
        this.discipline = discipline;
        this.summary = summary;
        this.relevanceScore = relevanceScore;
        this.additionalData = additionalData;
        this.createdAt = Instant.now();
    }
}
