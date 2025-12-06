package app;

public class AppManager {

    private static volatile boolean running = true;

    public static void main(String[] args) {
        System.out.println("AppManager gestartet – Shopmanager ist bereit.");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                running = false;
                System.out.println("AppManager wird beendet …");
            } catch (Exception ignored) {}
        }, "shutdown-hook"));

        InputListener inputListener = new InputListener();
        BackgroundScheduler scheduler = new BackgroundScheduler();

        inputListener.setDaemon(true);
        scheduler.setDaemon(true);
        inputListener.start();
        scheduler.start();

        while (running) {
            try {
                Thread.sleep(1000); // Hauptschleife hält alles am Leben
            } catch (InterruptedException e) {
                running = false;
            }
        }
        System.out.println("AppManager beendet.");
    }
}

class InputListener extends Thread {
    @Override
    public void run() {
        while (!isInterrupted()) {
            // Simuliert Eingabeüberwachung – könnte an Touch‑Ereignisse gebunden werden
            try {
                Thread.sleep(3000);
                System.out.println("[InputListener] Überwache Benutzerinteraktionen…");
            } catch (InterruptedException e) {
                interrupt();
            }
        }
    }
}

class BackgroundScheduler extends Thread {
    @Override
    public void run() {
        while (!isInterrupted()) {
            try {
                Thread.sleep(60_000); // alle 60 Sekunden Ping
                System.out.println("[BackgroundScheduler] Session wach gehalten.");
                // Hier könnte man API‑Ping oder Initialisierungsaufruf einbauen
            } catch (InterruptedException e) {
                interrupt();
            }
        }
    }
}
