package com.june.demo;

import com.june.robotics.swarm.AdvancedSwarmRobot;
import lombok.extern.slf4j.Slf4j;

/**
 * 🦊 Advanced Swarm Robotics Demo
 * Demonstriert vollständig integrierte Schwarmroboter
 */
@Slf4j
public class AdvancedSwarmDemo {

    public static void main(String[] args) {
        log.info("═══════════════════════════════════════════════════════════");
        log.info("🦊 ADVANCED SWARM ROBOTICS SYSTEM DEMO");
        log.info("═══════════════════════════════════════════════════════════\n");

        // Erstelle drei Advanced Swarm Roboter
        AdvancedSwarmRobot robot1 = new AdvancedSwarmRobot("ALPHA-001");
        AdvancedSwarmRobot robot2 = new AdvancedSwarmRobot("BETA-002");
        AdvancedSwarmRobot robot3 = new AdvancedSwarmRobot("GAMMA-003");

        log.info("\n--- INITIALISIERUNG ABGESCHLOSSEN ---\n");

        // Teste Roboter-Bereitschaft
        testRobotReadiness(robot1);
        testRobotReadiness(robot2);
        testRobotReadiness(robot3);

        log.info("\n--- ERSTE OPERATIONS-PHASE ---\n");
        robot1.operate();
        robot2.operate();
        robot3.operate();

        log.info("\n--- SYSTEM-OPTIMIERUNG ---\n");
        robot1.optimizeSystems();
        robot2.optimizeSystems();
        robot3.optimizeSystems();

        log.info("\n--- ZWEITE OPERATIONS-PHASE ---\n");
        robot1.operate();
        robot2.operate();
        robot3.operate();

        log.info("\n--- VERTEIDIGUNGSMODUS TEST ---\n");
        robot1.activateDefenseMode();
        robot2.activateDefenseMode();

        log.info("\n--- DRITTE OPERATIONS-PHASE ---\n");
        robot1.operate();
        robot2.operate();
        robot3.operate();

        // Finale Status-Reports
        log.info("\n═══════════════════════════════════════════════════════════");
        log.info("📊 FINALE SYSTEM-REPORTS");
        log.info("═══════════════════════════════════════════════════════════\n");

        System.out.println(robot1.getDetailedStatusReport());
        System.out.println();
        System.out.println(robot2.getDetailedStatusReport());
        System.out.println();
        System.out.println(robot3.getDetailedStatusReport());

        // Gesamtstatistik
        printOverallStatistics(robot1, robot2, robot3);

        log.info("\n═══════════════════════════════════════════════════════════");
        log.info("✅ DEMO ABGESCHLOSSEN - Öffne Dashboard...");
        log.info("═══════════════════════════════════════════════════════════");
    }

    private static void testRobotReadiness(AdvancedSwarmRobot robot) {
        log.info("🔍 Teste Roboter: {}", robot.getRobotId());

        if (robot.isReady()) {
            log.info("✅ Status: BEREIT - Alle Systeme operational");
        } else {
            robot.handleError("Roboter ist nicht bereit.");
        }
        log.info("");
    }

    private static void printOverallStatistics(AdvancedSwarmRobot... robots) {
        double totalEnergy = 0;
        double totalCO2 = 0;
        double totalChemicals = 0;
        double totalHours = 0;
        int totalCommands = 0;
        int activeMagFields = 0;

        log.info("\n╔════════════════════════════════════════════════════════════╗");
        log.info("║           🌟 GESAMT-ZUSAMMENFASSUNG                        ║");
        log.info("╠════════════════════════════════════════════════════════════╣");

        for (AdvancedSwarmRobot robot : robots) {
            totalEnergy += robot.getSolarPanel().getTotalEnergyProduced();
            totalCO2 += robot.getConverter().getCO2Reduction();
            totalChemicals += robot.getConverter().getTotalChemicalsProduced();
            totalHours += robot.getOperatingHours();
            totalCommands += robot.getCommunicator().getCommandsReceived();
            if (robot.getFieldGenerator().isActive()) activeMagFields++;
        }

        log.info("║  ⚡ Total Energie produziert:        {:>10.2f} kWh     ║", totalEnergy);
        log.info("║  🌿 Total CO₂ Reduktion:             {:>10.2f} kg      ║", totalCO2);
        log.info("║  🧪 Total Chemikalien produziert:    {:>10.2f} kg      ║", totalChemicals);
        log.info("║  ⏱️ Total Betriebsstunden:           {:>10.1f} h       ║", totalHours);
        log.info("║  📡 Total Befehle empfangen:         {:>10d}         ║", totalCommands);
        log.info("║  🧲 Aktive Magnetfelder:             {:>10d}         ║", activeMagFields);
        log.info("║  🤖 Roboter in Fleet:                {:>10d}         ║", robots.length);
        log.info("╠════════════════════════════════════════════════════════════╣");

        double avgEfficiency = totalEnergy / totalHours / robots.length;
        log.info("║  📊 Durchschnittliche Effizienz:     {:>10.2f} kWh/h   ║", avgEfficiency);
        log.info("║  🌍 Umweltauswirkung:                {:>10s}         ║", "POSITIV");
        log.info("╚════════════════════════════════════════════════════════════╝");
    }
}
