package com.june.de.nursystem.reconstruction;

/**
 * Gebäudetypen
 */
public enum BuildingType {
    RESIDENTIAL("Wohngebäude"),
    HOSPITAL("Krankenhaus"),
    SCHOOL("Schule"),
    COMMERCIAL("Gewerbe"),
    INDUSTRIAL("Industrie"),
    RELIGIOUS("Religiöse Stätte"),
    INFRASTRUCTURE("Infrastruktur");

    private final String displayName;

    BuildingType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
