package de.nursystem.visionaid;

import java.util.List;

/**
 * Therapie-Sitzung
 */
class TherapieSitzung {
    private final Patient patient;
    private final List<TherapieUebung> uebungen;
    private final int dauer;
    private int fortschritt;
    private boolean abgeschlossen;

    public TherapieSitzung(Patient patient, List<TherapieUebung> uebungen, int dauer) {
        this.patient = patient;
        this.uebungen = uebungen;
        this.dauer = dauer;
        this.fortschritt = 0;
        this.abgeschlossen = false;
    }

    public void updateFortschritt(int fortschritt) {
        this.fortschritt = fortschritt;
    }

    public void setAbgeschlossen(boolean abgeschlossen) {
        this.abgeschlossen = abgeschlossen;
        if (abgeschlossen) this.fortschritt = 100;
    }

    // Getter
    public Patient getPatient() { return patient; }
    public List<TherapieUebung> getUebungen() { return uebungen; }
    public int getDauer() { return dauer; }
    public int getFortschritt() { return fortschritt; }
    public boolean isAbgeschlossen() { return abgeschlossen; }
}
