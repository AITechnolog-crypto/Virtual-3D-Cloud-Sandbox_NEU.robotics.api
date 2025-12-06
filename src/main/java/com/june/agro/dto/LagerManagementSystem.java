package com.june.agro;

import java.util.*;

/**
 * Hauptklasse für das Management des Lagerungssystems (Demo/Main).
 *
 * Hinweis: Diese Demo ist unabhängig vom bestehenden Spring Boot Projekt
 * und kann separat über die Main-Methode gestartet werden.
 */
public class LagerManagementSystem {
    private ILagerSystem<LandwirtschaftsProdukt> lagerSystem;

    public LagerManagementSystem() {
        lagerSystem = new GlobalesLagerSystem<>();
    }

    public void manageLager() {
        List<LandwirtschaftsProdukt> produkte = Arrays.asList(
                new LandwirtschaftsProdukt("Korn", 10000),
                new LandwirtschaftsProdukt("Mais", 5000),
                new LandwirtschaftsProdukt("Korn", 2000) // Fügt weitere 2000 Einheiten Korn hinzu
        );

        lagerSystem.lagereProdukte(produkte);

        try {
            List<LandwirtschaftsProdukt> kornProdukte = lagerSystem.getProdukte("Korn");
            System.out.println("Gelagerte Korn-Produkte:");
            kornProdukte.forEach(System.out::println);

            System.out.println(lagerSystem.getLagerBestand());

            // Erwartet: wirft eine ProduktNichtGefundenException
            lagerSystem.getProdukte("Weizen");
        } catch (ProduktNichtGefundenException e) {
            System.err.println("Fehler: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        LagerManagementSystem managementSystem = new LagerManagementSystem();
        managementSystem.manageLager();
    }
}
