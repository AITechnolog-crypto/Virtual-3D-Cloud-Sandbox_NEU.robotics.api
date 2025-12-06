package com.june.model.painrelief;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Behandlungsplan {
    private String methode;
    private double dosierung;
    private String zielbereich;

    public Behandlungsplan(String methode, double dosierung, String zielbereich) {
        this.methode = methode;
        this.dosierung = dosierung;
        this.zielbereich = zielbereich;
    }
}
