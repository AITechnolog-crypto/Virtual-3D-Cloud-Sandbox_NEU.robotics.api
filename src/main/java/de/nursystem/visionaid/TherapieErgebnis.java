package de.nursystem.visionaid;

/**
 * Therapie-Ergebnis
 */
class TherapieErgebnis {
    private final String patientName;
    private final int sitzungsDauer;
    private final double verbesserung;
    private final String empfehlungen;

    public TherapieErgebnis(String patientName, int sitzungsDauer,
                           double verbesserung, String empfehlungen) {
        this.patientName = patientName;
        this.sitzungsDauer = sitzungsDauer;
        this.verbesserung = verbesserung;
        this.empfehlungen = empfehlungen;
    }

    // Getter
    public String getPatientName() { return patientName; }
    public int getSitzungsDauer() { return sitzungsDauer; }
    public double getVerbesserung() { return verbesserung; }
    public String getEmpfehlungen() { return empfehlungen; }
}
