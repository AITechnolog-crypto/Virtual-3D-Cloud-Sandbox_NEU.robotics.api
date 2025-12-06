package de.nursystem.reconstruction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reconstruction Drone Application
 * Wiederaufbau-Drohnen-System für friedlichen Wiederaufbau
 *
 * Bismillahirahmanirahim ❤️
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ReconstructionDroneApplication {

    private static final Logger logger = LoggerFactory.getLogger(ReconstructionDroneApplication.class);

    public static void main(String[] args) {
        logger.info("🏗️ Starte Reconstruction Drone Application...");
        logger.info("   Bismillahirahmanirahim - Im Namen Allahs, des Allerbarmers ❤️");

        SpringApplication.run(ReconstructionDroneApplication.class, args);

        logger.info("✅ Reconstruction Drone Application erfolgreich gestartet!");
    }

    @Bean
    public DroneControlService droneControlService() {
        logger.info("🔧 Initialisiere DroneControlService...");
        return new DroneControlService();
    }

    @Bean
    public AIModel aiModel() {
        logger.info("🧠 Initialisiere AI Model...");
        return new AIModel();
    }

    @Bean
    public DataProcessingUnit dataProcessingUnit() {
        logger.info("💾 Initialisiere Data Processing Unit...");
        return new DataProcessingUnit();
    }

    @Bean
    public SafetyProtocol safetyProtocol() {
        logger.info("🛡️ Initialisiere Safety Protocol...");
        return new SafetyProtocol();
    }
}
