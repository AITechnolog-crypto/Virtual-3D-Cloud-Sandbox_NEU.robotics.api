package de.nursystem.visionaid;

/**
 * Notfall-Meldung
 */
class NotfallMeldung {
    private final Position position;
    private final NotfallTyp typ;
    private final long zeitstempel;

    public NotfallMeldung(Position position, NotfallTyp typ, long zeitstempel) {
        this.position = position;
        this.typ = typ;
        this.zeitstempel = zeitstempel;
    }

    public Position getPosition() { return position; }
    public NotfallTyp getTyp() { return typ; }
    public long getZeitstempel() { return zeitstempel; }
}
