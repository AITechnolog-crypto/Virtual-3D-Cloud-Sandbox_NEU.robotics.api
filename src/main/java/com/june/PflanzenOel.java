package com.june;

class PflanzenOel implements Material {
    @Override
    public String getName() { return "Pflanzliches Wärmeträgeröl"; }
    @Override
    public MaterialType getType() { return MaterialType.PFLANZLICH; }
}
