package com.june.autonomy;

import com.june.autonomy.action.IAction;

/**
 * Schnittstelle für eine autonome Maschine mit positiver/nicht-negativer Ausrichtung.
 */
public interface IAutonomousMachine {
    /** Führt positive Aktionen aus (z. B. Hilfs- oder Schutzfunktionen). */
    void performPositiveActions();

    /** Verhindert negative Aktionen (z. B. Schadensvermeidung, Safeguards). */
    void preventNegativeActions();

    /**
     * Setzt den Aggressivitätsgrad im Bereich [0, 1].
     * 0 = defensiv/risikoarm, 1 = sehr aggressiv/risikoreich.
     */
    void setAggressiveness(double aggressiveness);

    /**
     * Neue überladene Variante: Führt genau eine Aktion aus, sofern sie nicht negativ ist.
     * Default-Implementierung ruft bei nicht-negativer Aktion die Alt-API performPositiveActions() auf,
     * um Rückwärtskompatibilität zu gewährleisten.
     */
    default void performPositiveActions(IAction action) {
        if (action == null || action.isNegative()) {
            return; // NOP bei fehlender oder negativer Aktion
        }
        performPositiveActions();
    }
}