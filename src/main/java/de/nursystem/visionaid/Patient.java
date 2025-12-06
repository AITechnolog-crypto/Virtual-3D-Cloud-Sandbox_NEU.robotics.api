package de.nursystem.visionaid;

/**
 * Platzhalter für Patient
 */
class Patient {
    private final String name;
    private final Position position;

    public Patient(String name, Position position) {
        this.name = name;
        this.position = position;
    }

    public String getName() { return name; }
    public Position getPosition() { return position; }
}
