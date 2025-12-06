package de.nursystem.warehouse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service für die Verteilung von Produkten
 *
 * Bismillahirahmanirahim ❤️
 */
@Service
public class VerteilungsService {

    private static final Logger logger = LoggerFactory.getLogger(VerteilungsService.class);

    private List<Verteilung> verteilungsHistorie;

    public VerteilungsService() {
        this.verteilungsHistorie = new ArrayList<>();
    }

    /**
     * Verteilt Produkte basierend auf Anforderungen
     */
    public void verteileNachAnforderung(ILagerverwaltung lager, String region,
                                       List<ProduktAnforderung> anforderungen) {
        logger.info("📦 Verteile Produkte an: {}", region);

        Verteilung verteilung = new Verteilung(region);
        boolean erfolg = true;

        for (ProduktAnforderung anforderung : anforderungen) {
            LandwirtschaftsProdukt produkt = lager.getProdukt(anforderung.getProduktName());

            if (produkt == null) {
                logger.warn("  ⚠️ {} nicht verfügbar", anforderung.getProduktName());
                erfolg = false;
                continue;
            }

            if (produkt.getMenge() < anforderung.getMenge()) {
                logger.warn("  ⚠️ {} - Nicht genug Menge (verfügbar: {}, benötigt: {})",
                    anforderung.getProduktName(), produkt.getMenge(), anforderung.getMenge());
                erfolg = false;
                continue;
            }

            // Menge reduzieren
            lager.produktMengeAktualisieren(
                anforderung.getProduktName(),
                -anforderung.getMenge()
            );

            verteilung.addProdukt(anforderung.getProduktName(), anforderung.getMenge());
            logger.info("  ✅ {} - {} Einheiten verteilt",
                anforderung.getProduktName(), anforderung.getMenge());
        }

        if (erfolg) {
            verteilung.setStatus(VerteilungsStatus.ABGESCHLOSSEN);
            logger.info("  🎉 Verteilung erfolgreich abgeschlossen!");
        } else {
            verteilung.setStatus(VerteilungsStatus.TEILWEISE);
            logger.warn("  ⚠️ Verteilung nur teilweise erfolgreich");
        }

        verteilungsHistorie.add(verteilung);
    }

    /**
     * Generiert Verteilungs-Bericht
     */
    public void generiereVerteilungsBericht() {
        logger.info("📊 === VERTEILUNGS-BERICHT ===");
        logger.info("   Gesamtanzahl Verteilungen: {}", verteilungsHistorie.size());

        int abgeschlossen = (int) verteilungsHistorie.stream()
            .filter(v -> v.getStatus() == VerteilungsStatus.ABGESCHLOSSEN)
            .count();

        logger.info("   ✅ Erfolgreich: {}", abgeschlossen);
        logger.info("   ⚠️ Teilweise: {}", verteilungsHistorie.size() - abgeschlossen);
        logger.info("==============================");
    }

    public List<Verteilung> getVerteilungsHistorie() {
        return new ArrayList<>(verteilungsHistorie);
    }
}
