package de.nursystem.reconstruction;

/**
 * Konstruktionsdaten
 */
class ConstructionData {
    private final String siteName;
    private final Blueprint blueprint;
    private final Coordinates3D coordinates;
    private final MaterialProperties materials;

    public ConstructionData(String siteName, Blueprint blueprint,
                           Coordinates3D coordinates, MaterialProperties materials) {
        this.siteName = siteName;
        this.blueprint = blueprint;
        this.coordinates = coordinates;
        this.materials = materials;
    }

    public String getSiteName() { return siteName; }
    public Blueprint getBlueprint() { return blueprint; }
    public Coordinates3D getCoordinates() { return coordinates; }
    public MaterialProperties getMaterials() { return materials; }
}
