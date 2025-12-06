package de.nursystem.visionaid;

/**
 * Flotten-Bericht
 */
class FlottenBericht {
    private final int gesamt;
    private final int verfuegbar;
    private final int imEinsatz;
    private final int inWartung;
    private final FlottenStatus status;

    public FlottenBericht(int gesamt, int verfuegbar, int imEinsatz, int inWartung, FlottenStatus status) {
        this.gesamt = gesamt;
        this.verfuegbar = verfuegbar;
        this.imEinsatz = imEinsatz;
        this.inWartung = inWartung;
        this.status = status;
    }

    public int getGesamt() { return gesamt; }
    public int getVerfuegbar() { return verfuegbar; }
    public int getImEinsatz() { return imEinsatz; }
    public int getInWartung() { return inWartung; }
    public FlottenStatus getStatus() { return status; }
}
