package com.june.simulation;

import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DroneProductionSystemV2 {
    private static final Logger logger = Logger.getLogger(DroneProductionSystemV2.class.getName());
    private static final long TARGET_DRONE_COUNT = 1_000_000_000L;
    private final LongAdder producedDrones = new LongAdder();
    private final ExecutorService productionLine = Executors.newFixedThreadPool(10);
    private volatile boolean productionRunning = true; // Flag zum Stoppen der Produktion

    public void startProduction() {
        for (int i = 0; i < 10; i++) {
            productionLine.execute(this::produceDrone);
        }
    }

    private void produceDrone() {
        while (productionRunning && producedDrones.sum() < TARGET_DRONE_COUNT) {
            try {
                // Simulierte Produktionszeit
                Thread.sleep(1);
                producedDrones.increment();
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "Produktion unterbrochen.", e);
                Thread.currentThread().interrupt(); // Interrupt-Flag wieder setzen
                productionRunning = false; // Produktion stoppen
            }
        }
    }

    public void stopProduction() {
        productionRunning = false;
        productionLine.shutdown(); // Initiates an orderly shutdown
        try {
            if (!productionLine.awaitTermination(60, TimeUnit.SECONDS)) {
                productionLine.shutdownNow(); // Forcefully shutdown
                if (!productionLine.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ex) {
            productionLine.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public void printProductionStatus() {
        while (producedDrones.sum() < TARGET_DRONE_COUNT && productionRunning) {
            try {
                Thread.sleep(1000); // Warte 1 Sekunde
                long currentCount = producedDrones.sum();
                double progress = (double) currentCount / TARGET_DRONE_COUNT * 100;
                logger.info("Produktionsfortschritt: " + currentCount + " / " + TARGET_DRONE_COUNT + " (" + String.format("%.2f", progress) + "%)");
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "Statusanzeige unterbrochen.", e);
                Thread.currentThread().interrupt();
                return;
            }
        }
        logger.info("Produktion abgeschlossen. Insgesamt produzierte Drohnen: " + producedDrones.sum());
    }

    public static void main(String[] args) throws InterruptedException {
        DroneProductionSystemV2 productionSystem = new DroneProductionSystemV2();
        productionSystem.startProduction();
        productionSystem.printProductionStatus();
        Thread.sleep(5000);
        productionSystem.stopProduction();
    }
}
