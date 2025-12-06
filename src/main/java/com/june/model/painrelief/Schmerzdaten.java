package com.june.model.painrelief;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Schmerzdaten {
    private double intensitaet;
    private String lokalisierung;
    private String typ;

    public Schmerzdaten(double intensitaet, String lokalisierung, String typ) {
        this.intensitaet = intensitaet;
        this.lokalisierung = lokalisierung;
        this.typ = typ;
    }
}
