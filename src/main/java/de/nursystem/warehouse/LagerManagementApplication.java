package de.nursystem.warehouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Globales Lagerverwaltungssystem für landwirtschaftliche Produkte
 *
 * Bismillahirahmanirahim ❤️
 */
@SpringBootApplication
@EnableDiscoveryClient
public class LagerManagementApplication {

    private static final Logger logger = LoggerFactory.getLogger(LagerManagementApplication.class);

    public static void main(String[] args) {
        logger.info("🌾 Starte Globales Lagerverwaltungssystem...");
        logger.info("   Bismillahirahmanirahim - Für die weltweite Nahrungsmittel-Sicherheit ❤️");

        SpringApplication.run(LagerManagementApplication.class, args);

        logger.info("✅ Lagerverwaltungssystem erfolgreich gestartet!");
    }

    @Bean
    public LagerManagementSystem lagerManagementSystem() {
        logger.info("🔧 Initialisiere Lager-Management-System...");
        return new LagerManagementSystem();
    }

    @Bean
    public ILagerverwaltung lagerverwaltung() {
        logger.info("📦 Initialisiere Lagerverwaltung...");
        return new GlobalesLagerverwaltungssystem();
    }

    @Bean
    public VerteilungsService verteilungsService() {
        logger.info("🚚 Initialisiere Verteilungs-Service...");
        return new VerteilungsService();
    }

    @Bean
    public BestandsAnalyseService bestandsAnalyseService() {
        logger.info("📊 Initialisiere Bestands-Analyse-Service...");
        return new BestandsAnalyseService();
    }
}
