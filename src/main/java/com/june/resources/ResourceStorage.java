package com.june.resources;

import java.util.EnumMap;
import java.util.Map;

/**
 * Einfacher Ressourcen-Speicher mit Bewertung je Einheit.
 * Entspricht der Beschreibung: Zuweisung, Abfrage, Freigabe mit Exception und Gesamtwert.
 */
public class ResourceStorage {

    private final Map<ResourceType, Integer> resources;

    public ResourceStorage() {
        this.resources = new EnumMap<>(ResourceType.class);
        for (ResourceType type : ResourceType.values()) {
            resources.put(type, 0);
        }
    }

    /**
     * Fügt eine Anzahl Einheiten eines Ressourcentyps hinzu. Negative/0 wird abgewiesen.
     * @return true, falls erfolgreich zugewiesen wurde; false bei ungültiger Menge
     */
    public boolean allocateResources(ResourceType resourceType, int quantity) {
        if (resourceType == null || quantity <= 0) {
            return false;
        }
        resources.put(resourceType, resources.get(resourceType) + quantity);
        return true;
    }

    /**
     * Liefert gespeicherte Einheitenzahl des Typs.
     */
    public int getResourceUnits(ResourceType resourceType) {
        if (resourceType == null) return 0;
        return resources.getOrDefault(resourceType, 0);
    }

    /**
     * Gibt eine Anzahl Einheiten frei (subtrahiert). Wirft bei unzureichendem Bestand eine Exception.
     */
    public void releaseResources(ResourceType resourceType, int quantity) throws InsufficientResourcesException {
        if (resourceType == null || quantity <= 0) return; // no-op für ungültige Parameter
        int available = resources.getOrDefault(resourceType, 0);
        if (available < quantity) {
            throw new InsufficientResourcesException(resourceType.name(), quantity, available);
        }
        resources.put(resourceType, available - quantity);
    }

    /**
     * Berechnet den Gesamtwert aller Ressourcen.
     */
    public int getTotalValue() {
        int totalValue = 0;
        for (Map.Entry<ResourceType, Integer> entry : resources.entrySet()) {
            totalValue += entry.getKey().getValue() * entry.getValue();
        }
        return totalValue;
    }

    /**
     * Ressourcentypen mit Wert je Einheit.
     */
    public enum ResourceType {
        GOLD(10),
        PLATINUM(25);

        private final int value;
        ResourceType(int value) { this.value = value; }
        public int getValue() { return value; }
    }

    /**
     * Kleiner Demo-Entry-Point entsprechend der Beschreibung.
     */
    public static void main(String[] args) {
        ResourceStorage storage = new ResourceStorage();
        storage.allocateResources(ResourceType.GOLD, 100);
        storage.allocateResources(ResourceType.PLATINUM, 50);

        System.out.println("Gold: " + storage.getResourceUnits(ResourceType.GOLD));
        System.out.println("Platin: " + storage.getResourceUnits(ResourceType.PLATINUM));
        System.out.println("Gesamtwert: " + storage.getTotalValue());

        try {
            storage.releaseResources(ResourceType.GOLD, 150); // Sollte Exception werfen
        } catch (InsufficientResourcesException e) {
            System.err.println(e.getMessage());
        }

        try {
            storage.releaseResources(ResourceType.GOLD, 50);
            System.out.println("Gold nach Freigabe: " + storage.getResourceUnits(ResourceType.GOLD));
        } catch (InsufficientResourcesException e) {
            System.err.println(e.getMessage());
        }
    }
}
