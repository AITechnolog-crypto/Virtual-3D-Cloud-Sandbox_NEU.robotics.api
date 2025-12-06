package de.nursystem.visionaid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Verwaltung der Tarn-Drohnenflotte für medizinische Lieferungen
 *
 * Bismillahirahmanirahim ❤️
 */
@Component
public class DrohnenFlotte {

    private static final Logger logger = LoggerFactory.getLogger(DrohnenFlotte.class);

    private List<TarnDrohne> drohnen;
    private int drohnenZaehler;
    private FlottenStatus status;

    public DrohnenFlotte() {
        this.drohnen = new ArrayList<>();
        this.drohnenZaehler = 0;
        this.status = FlottenStatus.NICHT_BEREIT;
        logger.info("🚁 Drohnenflotte erstellt");
    }

    /**
     * Bereitet die Drohnenflotte vor
     */
    public void bereiteDrohnenVor() throws DrohnenVorbereitungsException {
        logger.info("🔧 Bereite Drohnenflotte vor...");

        try {
            // Erstelle initiale Drohnenflotte
            erstelleInitialeDrohnen(10);

            // Führe System-Checks durch
            fuehreDiagnoseDurch();

            // Kalibriere Sensoren
            kalibriereSensoren();

            // Aktiviere Tarnmodus
            aktiviereTarnmodus();

            status = FlottenStatus.BEREIT;
            logger.info("  ✅ Drohnenflotte bereit - {} Drohnen einsatzfähig",
                getVerfuegbareDrohnen().size());

        } catch (Exception e) {
            status = FlottenStatus.FEHLER;
            throw new DrohnenVorbereitungsException(
                "Fehler bei der Drohnenvorbereitung: " + e.getMessage()
            );
        }
    }

    /**
     * Wählt optimale Drohne für Route
     */
    public TarnDrohne waehleOptimaleDrohne(Lieferroute route) {
        logger.debug("🔍 Wähle optimale Drohne für Route...");

        List<TarnDrohne> verfuegbar = getVerfuegbareDrohnen();

        if (verfuegbar.isEmpty()) {
            logger.warn("⚠️ Keine Drohnen verfügbar - erstelle neue");
            erstelleNeueDrohne();
            verfuegbar = getVerfuegbareDrohnen();
        }

        // Wähle Drohne mit höchster Batterieladung
        TarnDrohne beste = verfuegbar.get(0);
        for (TarnDrohne drohne : verfuegbar) {
            if (drohne.getBatterieLadung() > beste.getBatterieLadung()) {
                beste = drohne;
            }
        }

        logger.debug("  ✅ Drohne {} ausgewählt (Batterie: {}%)",
            beste.getId(), String.format("%.0f", beste.getBatterieLadung()));

        return beste;
    }

    /**
     * Sendet Notfall-Drohne zum Ziel
     */
    public TarnDrohne sendeNotfallDrohne(Position ziel) {
        logger.warn("🚨 Sende Notfall-Drohne zu: {}", ziel);

        // Wähle schnellste verfügbare Drohne
        TarnDrohne notfallDrohne = waehleSchnellsteDrohne();

        if (notfallDrohne == null) {
            logger.error("❌ Keine Drohnen verfügbar - erstelle Notfall-Drohne");
            notfallDrohne = erstelleNotfallDrohne();
        }

        // Aktiviere Notfall-Modus
        notfallDrohne.aktiviereNotfallModus();

        // Berechne schnellste Route
        notfallDrohne.setzeZiel(ziel);

        logger.warn("  ⚡ Notfall-Drohne {} unterwegs", notfallDrohne.getId());

        return notfallDrohne;
    }

    /**
     * Führt Diagnose aller Drohnen durch
     */
    public void fuehreDiagnoseDurch() {
        logger.info("🔬 Führe Flotten-Diagnose durch...");

        int gesund = 0;
        int wartung = 0;
        int defekt = 0;

        for (TarnDrohne drohne : drohnen) {
            DrohnenGesundheit gesundheit = drohne.pruefGeundheit();

            switch (gesundheit) {
                case OPTIMAL:
                case GUT:
                    gesund++;
                    break;
                case WARTUNG_ERFORDERLICH:
                    wartung++;
                    logger.warn("  ⚠️ Drohne {} benötigt Wartung", drohne.getId());
                    break;
                case DEFEKT:
                    defekt++;
                    logger.error("  ❌ Drohne {} ist defekt", drohne.getId());
                    break;
            }
        }

        logger.info("  📊 Diagnose-Ergebnis:");
        logger.info("     ✅ Gesund: {}", gesund);
        logger.info("     ⚠️ Wartung: {}", wartung);
        logger.info("     ❌ Defekt: {}", defekt);
    }

    /**
     * Gibt Status-Bericht der Flotte
     */
    public FlottenBericht generiereFlottenBericht() {
        int verfuegbar = getVerfuegbareDrohnen().size();
        int imEinsatz = getImEinsatzDrohnen().size();
        int inWartung = getWartungDrohnen().size();

        return new FlottenBericht(
            drohnen.size(),
            verfuegbar,
            imEinsatz,
            inWartung,
            status
        );
    }

    // Private Hilfsmethoden
    private void erstelleInitialeDrohnen(int anzahl) {
        logger.debug("  🏭 Erstelle {} Drohnen...", anzahl);

        for (int i = 0; i < anzahl; i++) {
            erstelleNeueDrohne();
        }
    }

    private TarnDrohne erstelleNeueDrohne() {
        String id = "VD-" + String.format("%04d", ++drohnenZaehler);
        TarnDrohne drohne = new TarnDrohne(id);
        drohnen.add(drohne);

        logger.debug("    ✅ Drohne {} erstellt", id);
        return drohne;
    }

    private TarnDrohne erstelleNotfallDrohne() {
        TarnDrohne drohne = erstelleNeueDrohne();
        drohne.setBatterieLadung(100.0);
        drohne.aktiviereNotfallModus();
        return drohne;
    }

    private void kalibriereSensoren() {
        logger.debug("  🎯 Kalibriere Sensoren...");
        drohnen.forEach(TarnDrohne::kalibriereSensoren);
    }

    private void aktiviereTarnmodus() {
        logger.debug("  🔒 Aktiviere Tarnmodus für alle Drohnen...");
        drohnen.forEach(d -> d.setTarnmodus(true));
    }

    private TarnDrohne waehleSchnellsteDrohne() {
        return getVerfuegbareDrohnen().stream()
            .filter(d -> d.getBatterieLadung() > 50)
            .findFirst()
            .orElse(null);
    }

    private List<TarnDrohne> getVerfuegbareDrohnen() {
        return drohnen.stream()
            .filter(d -> d.getStatus() == DrohnenStatus.VERFUEGBAR)
            .toList();
    }

    private List<TarnDrohne> getImEinsatzDrohnen() {
        return drohnen.stream()
            .filter(d -> d.getStatus() == DrohnenStatus.IM_EINSATZ)
            .toList();
    }

    private List<TarnDrohne> getWartungDrohnen() {
        return drohnen.stream()
            .filter(d -> d.getStatus() == DrohnenStatus.WARTUNG)
            .toList();
    }

    // Getter
    public List<TarnDrohne> getDrohnen() { return new ArrayList<>(drohnen); }
    public FlottenStatus getStatus() { return status; }
}
