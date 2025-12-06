package de.nursystem.reconstruction;

/**
 * Repräsentiert eine Bauaufgabe
 */
class BuildingTask {
    private final String description;
    private final TaskType type;

    public BuildingTask(String description, TaskType type) {
        this.description = description;
        this.type = type;
    }

    public String getDescription() { return description; }
    public TaskType getType() { return type; }
}
