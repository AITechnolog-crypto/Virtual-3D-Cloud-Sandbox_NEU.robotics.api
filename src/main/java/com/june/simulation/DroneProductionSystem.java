package com.june.simulation;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Multithreaded Drone Production demo.
 *
 * Behavior mirrors the provided issue description: uses a fixed thread pool sized to available
 * processors, increments a global AtomicLong counter, simulates production latency (10ms),
 * logs progress per production line, and shuts down gracefully.
 *
 * This class is self-contained and does not depend on Spring components.
 */
public class DroneProductionSystem {
    private final Logger logger = Logger.getLogger(DroneProductionSystem.class.getName());
    private final ExecutorService productionService;
    private final AtomicLong droneCount;
    private final int numberOfCores;

    public DroneProductionSystem() {
        this.numberOfCores = Runtime.getRuntime().availableProcessors();
        this.productionService = Executors.newFixedThreadPool(numberOfCores);
        this.droneCount = new AtomicLong(0);
        logger.info("Starte Drohnenproduktion mit " + numberOfCores + " Produktionslinien.");
    }

    public void startProduction(long targetDroneCount) {
        for (int i = 0; i < numberOfCores; i++) {
            productionService.execute(() -> {
                long localCount = 0;
                while (droneCount.get() < targetDroneCount) {
                    droneCount.incrementAndGet();
                    localCount++;
                    try {
                        // Simuliere die Produktion einer Drohne (10ms Pause)
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        logger.log(Level.WARNING, "Thread wurde unterbrochen", e);
                        Thread.currentThread().interrupt();
                        break;
                    }
                    if (localCount % 10000 == 0) {
                        logger.info("Produktionslinie hat " + localCount + " Drohnen produziert.");
                    }
                }
                logger.info("Produktionslinie beendet. Insgesamt " + localCount + " Drohnen produziert.");
            });
        }
        try {
            productionService.shutdown();
            productionService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Das Herunterfahren des ExecutorService wurde unterbrochen", e);
            Thread.currentThread().interrupt();
        }
        logger.info("Produktion abgeschlossen. Insgesamt " + droneCount.get() + " Drohnen produziert.");
    }

    public static void main(String[] args) {
        DroneProductionSystem system = new DroneProductionSystem();
        system.startProduction(1_000_000L); // Ziel von 1 Million Drohnen (angepasst für Testzwecke)
    }
}
