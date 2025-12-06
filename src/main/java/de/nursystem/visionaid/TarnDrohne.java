package de.nursystem.visionaid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Tarn-Drohne für medizinische Lieferungen
 */
class TarnDrohne {
    private static final Logger logger = LoggerFactory.getLogger(TarnDrohne.class);

    private final String id;
    private double batterieLadung;
    private DrohnenStatus status;
    private boolean tarnmodus;
    private boolean notfallModus;
    private Position aktuellePosition;
    private Position zielPosition;
    private List<VisuelleHilfe> ladung;
    private int eta; // Geschätzte Ankunftszeit in Minuten

    public TarnDrohne(String id) {
        this.id = id;
        this.batterieLadung = 100.0;
        this.status = DrohnenStatus.VERFUEGBAR;
        this.tarnmodus = false;
        this.notfallModus = false;
        this.ladung = new ArrayList<>();
    }

    public void ladeHilfsmittel(List<VisuelleHilfe> hilfsmittel) {
        this.ladung.addAll(hilfsmittel);
        logger.debug("  📦 {} Hilfsmittel geladen auf Drohne {}", hilfsmittel.size(), id);
    }

    public void starteLieferung(Lieferroute route, Patient patient) {
        logger.info("  🚁 Drohne {} startet zu: {}", id, patient.getName());
        this.status = DrohnenStatus.IM_EINSATZ;
        this.zielPosition = route.getZiel();
        this.eta = route.getGeschaetzteZeit();

        // Simuliere Flug
        fliegen(route);
    }

    public void aktiviereNotfallModus() {
        this.notfallModus = true;
        this.status = DrohnenStatus.NOTFALL;
        logger.warn("  🚨 Notfall-Modus aktiviert für Drohne {}", id);
    }

    public void setzeZiel(Position ziel) {
        this.zielPosition = ziel;
        this.eta = berechneETA(ziel);
    }

    public DrohnenGesundheit pruefGeundheit() {
        if (batterieLadung < 20) return DrohnenGesundheit.WARTUNG_ERFORDERLICH;
        if (batterieLadung < 50) return DrohnenGesundheit.GUT;
        return DrohnenGesundheit.OPTIMAL;
    }

    public void kalibriereSensoren() {
        // Simuliere Sensor-Kalibrierung
    }

    private void fliegen(Lieferroute route) {
        // Simuliere Flug entlang der Route
        batterieLadung -= route.getDistanz() * 0.5;
    }

    private int berechneETA(Position ziel) {
        // Vereinfachte ETA-Berechnung
        return (int) (10 + Math.random() * 20); // 10-30 Minuten
    }

    // Getter und Setter
    public String getId() { return id; }
    public double getBatterieLadung() { return batterieLadung; }
    public void setBatterieLadung(double ladung) { this.batterieLadung = ladung; }
    public DrohnenStatus getStatus() { return status; }
    public void setTarnmodus(boolean tarnmodus) { this.tarnmodus = tarnmodus; }
    public int getETA() { return eta; }
}
