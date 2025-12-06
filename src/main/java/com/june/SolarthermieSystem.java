package com.june;

// Hauptklasse für das Solarthermie-System
class SolarthermieSystem {
    private Material isolation;
    private Material waermetraegerfluessigkeit;
    private Material strukturelleKomponente;
    private Material waermeabsorber;

    // Konstruktor, der die Materialien übergeben bekommt (Dependency Injection)
    public SolarthermieSystem(Material isolation, Material waermetraeger, Material struktur, Material absorber) {
        this.isolation = isolation;
        this.waermetraegerfluessigkeit = waermetraeger;
        this.strukturelleKomponente = struktur;
        this.waermeabsorber = absorber;
        System.out.println("Solarthermie-System konfiguriert mit:");
        System.out.println("- Isolation: " + isolation.getName() + " (" + isolation.getType() + ")");
        System.out.println("- Wärmeträger: " + waermetraeger.getName() + " (" + waermetraeger.getType() + ")");
        System.out.println("- Struktur: " + struktur.getName() + " (" + struktur.getType() + ")");
        System.out.println("- Absorber: " + absorber.getName() + " (" + absorber.getType() + ")");
    }

    public void isoliereKollektoren() {
        if (isolation.getType() == MaterialType.PFLANZLICH) {
            System.out.println("Aktion: Kollektoren werden mit nachhaltigem Material '" + isolation.getName() + "' isoliert.");
        } else {
            System.out.println("Aktion: Kollektoren werden mit '" + isolation.getName() + "' isoliert.");
        }
    }

    public void fülleSystem() {
        System.out.println("Aktion: System wird mit '" + waermetraegerfluessigkeit.getName() + "' als Wärmeträgerflüssigkeit befüllt.");
    }



    public void baueStruktur() {
        System.out.println("Aktion: Strukturelle Komponenten werden aus '" + strukturelleKomponente.getName() + "' gebaut.");
    }

    public void beschichteAbsorber() {
        System.out.println("Aktion: Wärmeabsorber werden mit '" + waermeabsorber.getName() + "' beschichtet.");
    }
}
