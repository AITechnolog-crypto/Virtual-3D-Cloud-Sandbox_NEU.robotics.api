package com.june.simulation;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Multithreaded Drohnenproduktion basierend auf LongAdder und fester Zielmenge.
 * Diese Klasse ist additive und beeinflusst keine bestehenden Komponenten.
 */
public class DroneProductionSystemLongAdder {
    private static final long TARGET_DRONE_COUNT = 1_000_000_000L; // 1 Milliarde
    private final LongAdder producedDrones = new LongAdder();
    private final ExecutorService productionLine;
    private final Logger logger = Logger.getLogger(DroneProductionSystemLongAdder.class.getName());
    private final int numberOfCores;

    public DroneProductionSystemLongAdder() {
        this.numberOfCores = Runtime.getRuntime().availableProcessors();
        this.productionLine = Executors.newFixedThreadPool(numberOfCores);
        logger.info("Starte Drohnenproduktion mit " + numberOfCores + " Produktionslinien.");
    }

    public void startProduction() {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < numberOfCores; i++) {
            productionLine.execute(() -> {
                long localCount = 0;
                while (producedDrones.sum() < TARGET_DRONE_COUNT) {
                    producedDrones.increment();
                    localCount++;
                    try {
                        // Simuliere die Produktion einer Drohne (1 ms Pause - anpassbar)
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        logger.log(Level.WARNING, "Thread wurde unterbrochen", e);
                        Thread.currentThread().interrupt();
                        break;
                    }

                    // Logge alle 100.000 produzierten Drohnen pro Thread
                    if (localCount % 100_000 == 0) {
                        logger.info("Produktionslinie " + Thread.currentThread().getName() +
                                " hat " + localCount + " Drohnen produziert.");
                    }
                }
                logger.info("Produktionslinie " + Thread.currentThread().getName() +
                        " beendet. Insgesamt " + localCount + " Drohnen produziert.");
            });
        }

        try {
            productionLine.shutdown();
            productionLine.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Das Herunterfahren des ExecutorService wurde unterbrochen", e);
            Thread.currentThread().interrupt();
        }
        long endTime = System.currentTimeMillis();
        logger.info("Produktion abgeschlossen. Insgesamt " + producedDrones.sum() + " Drohnen produziert in "
                + (endTime - startTime) + "ms.");
    }

    public static void main(String[] args) {
        DroneProductionSystemLongAdder productionSystem = new DroneProductionSystemLongAdder();
        productionSystem.startProduction();
    }
}
