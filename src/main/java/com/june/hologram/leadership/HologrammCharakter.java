package com.june.hologram.leadership;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 🎭 Abstrakte Hologramm-Charakter Basisklasse
 * Definiert grundlegende Eigenschaften für holographische Führungspersönlichkeiten
 */
@Data
@Slf4j
public abstract class HologrammCharakter {
    protected String name;
    protected String geschlecht;
    protected String moralischeEigenschaften;

    public HologrammCharakter(String name, String geschlecht, String moralischeEigenschaften) {
        this.name = name;
        this.geschlecht = geschlecht;
        this.moralischeEigenschaften = moralischeEigenschaften;
        log.info("🎭 Hologramm-Charakter {} erstellt ({})", name, geschlecht);
    }

    public abstract void zeigeEigenschaften();
    public abstract void spreche();
}
