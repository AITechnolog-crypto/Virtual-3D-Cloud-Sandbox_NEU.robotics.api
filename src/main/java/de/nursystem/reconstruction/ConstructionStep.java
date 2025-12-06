package de.nursystem.reconstruction;

/**
 * Bauablauf-Schritt
 */
class ConstructionStep {
    private final String name;
    private final int days;

    public ConstructionStep(String name, int days) {
        this.name = name;
        this.days = days;
    }

    public String getName() { return name; }
    public int getDays() { return days; }
}
