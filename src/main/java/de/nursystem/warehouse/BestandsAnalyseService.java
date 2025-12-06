package de.nursystem.warehouse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service für Bestands-Analyse
 */
@Service
public class BestandsAnalyseService {

    private static final Logger logger = LoggerFactory.getLogger(BestandsAnalyseService.class);

    /**
     * Analysiert den aktuellen Bestand
     */
    public void analysiereBestand(ILagerverwaltung lager) {
        logger.info("🔍 Analysiere Lagerbestand...");

        // Knappe Produkte
        GlobalesLagerverwaltungssystem globalLager = (GlobalesLagerverwaltungssystem) lager;
        List<LandwirtschaftsProdukt> knapp = globalLager.getKnappeProdukte();

        if (!knapp.isEmpty()) {
            logger.warn("  ⚠️ {} Produkte sind knapp:", knapp.size());
            knapp.forEach(p -> logger.warn("     - {} ({} Einheiten)",
                p.getName(), p.getMenge()));
        }

        // Abgelaufene Produkte
        List<LandwirtschaftsProdukt> abgelaufen = globalLager.getAbgelaufeneProdukte();

        if (!abgelaufen.isEmpty()) {
            logger.error("  ❌ {} Produkte sind abgelaufen:", abgelaufen.size());
            abgelaufen.forEach(p -> logger.error("     - {} (seit {})",
                p.getName(), p.getAblaufdatum()));
        }

        // Verteilung nach Kategorie
        Map<ProduktKategorie, Double> nachKategorie = lager.getMengeNachKategorie();
        logger.info("  📊 Verteilung nach Kategorie:");
        nachKategorie.forEach((k, m) ->
            logger.info("     {} {}: {}",
                getKategorieEmoji(k), k, String.format("%.0f Einheiten", m)));
    }

    /**
     * Erstellt detaillierten Lager-Bericht
     */
    public LagerBericht erstelleBericht(ILagerverwaltung lager) {
        GlobalesLagerverwaltungssystem globalLager = (GlobalesLagerverwaltungssystem) lager;

        return new LagerBericht(
            lager.getProdukte().size(),
            lager.getGesamtmenge(),
            globalLager.getKnappeProdukte().size(),
            globalLager.getAbgelaufeneProdukte().size(),
            lager.getMengeNachKategorie()
        );
    }

    /**
     * Berechnet optimale Nachbestellungsmengen
     */
    public Map<String, Double> berechneNachbestellungen(ILagerverwaltung lager) {
        Map<String, Double> nachbestellungen = new HashMap<>();

        GlobalesLagerverwaltungssystem globalLager = (GlobalesLagerverwaltungssystem) lager;
        List<LandwirtschaftsProdukt> knapp = globalLager.getKnappeProdukte();

        for (LandwirtschaftsProdukt produkt : knapp) {
            // Empfohlene Nachbestellung: 5000 - aktuelle Menge
            double empfohlene = 5000 - produkt.getMenge();
            nachbestellungen.put(produkt.getName(), empfohlene);
        }

        return nachbestellungen;
    }

    private String getKategorieEmoji(ProduktKategorie kategorie) {
        return switch (kategorie) {
            case GETREIDE -> "🌾";
            case GEMUESE -> "🥕";
            case OBST -> "🍎";
            case HUELSENFRUECHTE -> "🫘";
            case MILCHPRODUKTE -> "🥛";
            case FLEISCH -> "🥩";
            case GEWUERZE -> "🌶️";
            default -> "📦";
        };
    }
}
