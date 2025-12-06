package de.nursystem.reconstruction;

/**
 * Materialbedarf
 */
class MaterialRequirements {
    private final int concrete;
    private final int steel;
    private final int bricks;
    private final int wood;
    private final int insulation;
    private final int windowsDoors;

    public MaterialRequirements(int concrete, int steel, int bricks,
                               int wood, int insulation, int windowsDoors) {
        this.concrete = concrete;
        this.steel = steel;
        this.bricks = bricks;
        this.wood = wood;
        this.insulation = insulation;
        this.windowsDoors = windowsDoors;
    }

    // Getter...
}
