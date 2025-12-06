package de.nursystem.visionaid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VisionAidNet - Ein fortschrittliches System zur Unterstützung von Menschen mit Sehschwäche.
 * Dieses System nutzt Tarn-Drohnen und Satellitennetzwerke, um visuelle Hilfsmittel und therapeutische
 * Behandlungen an schwer zugängliche Orte zu liefern.
 *
 * Bismillahirahmanirahim ❤️
 */
@SpringBootApplication
@EnableDiscoveryClient
public class VisionAidNetApplication {

    private static final Logger logger = LoggerFactory.getLogger(VisionAidNetApplication.class);

    public static void main(String[] args) {
        logger.info("👁️ Starte VisionAidNet System...");
        logger.info("   Bismillahirahmanirahim - Für alle Menschen mit Sehschwäche ❤️");

        SpringApplication.run(VisionAidNetApplication.class, args);

        logger.info("✅ VisionAidNet System erfolgreich gestartet!");
    }

    @Bean
    public VisionAidNetSystem visionAidNetSystem() {
        logger.info("🔧 Initialisiere VisionAidNet System...");
        return new VisionAidNetSystem();
    }

    @Bean
    public SatellitenKommunikator satellitenKommunikator() {
        logger.info("🛰️ Initialisiere Satelliten-Kommunikator...");
        return new SatellitenKommunikator();
    }

    @Bean
    public DrohnenFlotte drohnenFlotte() {
        logger.info("🚁 Initialisiere Drohnenflotte...");
        return new DrohnenFlotte();
    }

    @Bean
    public VisuelleTherapieModul visuelleTherapieModul() {
        logger.info("🏥 Initialisiere Visuelles Therapie-Modul...");
        return new VisuelleTherapieModul();
    }

    @Bean
    public MedizinischeAusruestungService medizinischeAusruestungService() {
        logger.info("💊 Initialisiere Medizinische Ausrüstung Service...");
        return new MedizinischeAusruestungService();
    }
}
