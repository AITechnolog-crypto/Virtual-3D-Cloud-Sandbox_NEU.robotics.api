package com.june;

class PflanzlicheAbsorberBeschichtung implements Material {
    @Override
    public String getName() { return "Pflanzliche Absorber-Beschichtung"; }
    @Override
    public MaterialType getType() { return MaterialType.PFLANZLICH; }
}
