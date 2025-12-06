package com.june.de.nursystem.reconstruction;

/**
 * Ressourcen-Schätzung
 */
public class ResourceEstimate {
    private final int concrete;
    private final int steel;
    private final int bricks;
    private final int wood;

    public ResourceEstimate(int concrete, int steel, int bricks, int wood) {
        this.concrete = concrete;
        this.steel = steel;
        this.bricks = bricks;
        this.wood = wood;
    }

    public int getConcrete() { return concrete; }
    public int getSteel() { return steel; }
    public int getBricks() { return bricks; }
    public int getWood() { return wood; }

    @Override
    public String toString() {
        return String.format(
            "Benötigte Ressourcen:\n" +
            "  Beton: %d Tonnen\n" +
            "  Stahl: %d Tonnen\n" +
            "  Ziegel: %d Einheiten\n" +
            "  Holz: %d m³",
            concrete, steel, bricks, wood
        );
    }
}
