package de.nursystem.reconstruction;

/**
 * Material-Eigenschaften
 */
class MaterialProperties {
    private final String concreteType;
    private final String steelType;
    private final String brickType;

    public MaterialProperties(String concreteType, String steelType, String brickType) {
        this.concreteType = concreteType;
        this.steelType = steelType;
        this.brickType = brickType;
    }

    public String getConcreteType() { return concreteType; }
    public String getSteelType() { return steelType; }
    public String getBrickType() { return brickType; }
}
