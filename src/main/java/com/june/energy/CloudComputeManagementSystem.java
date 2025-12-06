package com.june.energy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * CloudComputeManagementSystem
 *
 * Analyse & Verbesserungsvorschläge:
 * - Entkopplung/Interfaces: Kollektoren (IEnergyCollector), Speicher (IEnergyStorage) und Compute-Center (IComputeCenter)
 *   sind über Schnittstellen abstrahiert. So lassen sich Implementierungen austauschen (z. B. andere Energiequellen, verteilte Speicher,
 *   echte Datacenter-Backends).
 * - Datenmodell: Energy ist immutable, somit threadsicherer bei Weitergabe zwischen Komponenten.
 * - Speicher: GlobalEnergyStorageSystem aggregiert Energiemengen pro Typ. Für höhere Lasten wäre eine
 *   concurrent-Datenstruktur oder Actor/Message-Ansatz denkbar.
 * - Fehlerbehandlung: collect/store/process sind aktuell „best-effort“. In der Praxis sollte Logging, Retry,
 *   Telemetrie/Metriken (z. B. Micrometer), sowie Circuit-Breaker eingesetzt werden.
 * - Konfiguration: Werte (z. B. Kollektorstärken) hart codiert – künftig extern konfigurierbar machen (Properties/DB/API).
 * - Skalierung: manageSystem() läuft synchron. Bei realen Workloads: Scheduler/Executor, Backpressure, asynchrone Pipelines.
 * - Testbarkeit: Jede Klasse ist klein und testbar. Unit-Tests könnten leicht hinzugefügt werden.
 */
public class CloudComputeManagementSystem {
    private final List<IEnergyCollector> energyCollectors;
    private final IEnergyStorage energyStorage;
    private final List<IComputeCenter> computeCenters;
    private final SecretTransportRailway secretRailway;

    public CloudComputeManagementSystem() {
        this.energyCollectors = new ArrayList<>();
        this.computeCenters = new ArrayList<>();
        // Initialisierung des Energiespeichers und der geheimen Transportbahn
        this.energyStorage = new GlobalEnergyStorageSystem();
        this.secretRailway = new SecretTransportRailway();
    }

    public void addEnergyCollector(IEnergyCollector collector) {
        if (collector != null) energyCollectors.add(collector);
    }

    public void addComputeCenter(IComputeCenter computeCenter) {
        if (computeCenter != null) computeCenters.add(computeCenter);
    }

    public void manageSystem() {
        // Sammeln und Speichern von Energie
        for (IEnergyCollector collector : energyCollectors) {
            try {
                Energy energy = collector.collectEnergy();
                energyStorage.storeEnergy(energy);
            } catch (RuntimeException ex) {
                System.err.println("Fehler beim Sammeln/Speichern von Energie: " + ex.getMessage());
            }
        }

        // Speicherstände ausgeben (nur Demo)
        if (energyStorage instanceof GlobalEnergyStorageSystem ges) {
            Map<EnergyType, Double> snap = ges.snapshot();
            System.out.println("Energie-Snapshot: " + snap + ", Total=" + ges.getTotal());
        }

        // Betrieb der Rechenzentren
        for (IComputeCenter center : computeCenters) {
            try {
                center.processTasks();
            } catch (RuntimeException ex) {
                System.err.println("Fehler bei der Auftragsverarbeitung: " + ex.getMessage());
            }
        }

        // Überprüfung auf Notfälle und Aktivierung der geheimen Transportbahn
        try {
            if (secretRailway.checkForEmergencies()) {
                secretRailway.activate();
            }
        } catch (RuntimeException ex) {
            System.err.println("Fehler bei Notfallprüfung/Transportbahn: " + ex.getMessage());
        }
    }

    // Hauptprogramm (Demo)
    public static void main(String[] args) {
        CloudComputeManagementSystem managementSystem = new CloudComputeManagementSystem();

        // Hinzufügen von Energiekollektoren
        managementSystem.addEnergyCollector(new SolarEnergyCollector());
        managementSystem.addEnergyCollector(new CO2EnergyCollector());

        // Hinzufügen von Rechenzentren an verschiedenen Standorten
        managementSystem.addComputeCenter(new ComputeCenter("Antarctica"));
        managementSystem.addComputeCenter(new ComputeCenter("Sahara"));

        // Starten des Managementsystems
        managementSystem.manageSystem();
    }
}
