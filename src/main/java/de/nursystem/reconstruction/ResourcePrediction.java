package de.nursystem.reconstruction;

/**
 * Ressourcen-Vorhersage
 */
class ResourcePrediction {
    private final int concrete;
    private final int steel;
    private final int bricks;
    private final int wood;

    public ResourcePrediction(int concrete, int steel, int bricks, int wood) {
        this.concrete = concrete;
        this.steel = steel;
        this.bricks = bricks;
        this.wood = wood;
    }

    public int getConcrete() { return concrete; }
    public int getSteel() { return steel; }
    public int getBricks() { return bricks; }
    public int getWood() { return wood; }
}
