package com.june.autonomy.action;

/**
 * Interface für eine ausführbare Aktion der autonomen Maschine.
 */
public interface IAction {
    /**
     * @return true, wenn die Aktion negativ ist und daher nicht ausgeführt werden soll.
     */
    boolean isNegative();

    /**
     * Beschreibung für Logging/Debugging.
     */
    String getDescription();
}
