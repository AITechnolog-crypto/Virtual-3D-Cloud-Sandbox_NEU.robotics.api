package de.nursystem.visionaid;

import java.util.List;

/**
 * Lieferroute
 */
class Lieferroute {
    private final Position ziel;
    private final double distanz;
    private final int geschaetzteZeit;
    private final List<Wegpunkt> wegpunkte;

    public Lieferroute(Position ziel, double distanz, int geschaetzteZeit, List<Wegpunkt> wegpunkte) {
        this.ziel = ziel;
        this.distanz = distanz;
        this.geschaetzteZeit = geschaetzteZeit;
        this.wegpunkte = wegpunkte;
    }

    public Position getZiel() { return ziel; }
    public double getDistanz() { return distanz; }
    public int getGeschaetzteZeit() { return geschaetzteZeit; }
    public List<Wegpunkt> getWegpunkte() { return wegpunkte; }
}
