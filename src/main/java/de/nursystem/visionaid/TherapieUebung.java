package de.nursystem.visionaid;

/**
 * Therapie-Übung
 */
class TherapieUebung {
    private final String name;
    private final String beschreibung;
    private final int dauer;
    private final Schwierigkeit schwierigkeit;

    public TherapieUebung(String name, String beschreibung, int dauer, Schwierigkeit schwierigkeit) {
        this.name = name;
        this.beschreibung = beschreibung;
        this.dauer = dauer;
        this.schwierigkeit = schwierigkeit;
    }

    // Getter
    public String getName() { return name; }
    public String getBeschreibung() { return beschreibung; }
    public int getDauer() { return dauer; }
    public Schwierigkeit getSchwierigkeit() { return schwierigkeit; }
}
