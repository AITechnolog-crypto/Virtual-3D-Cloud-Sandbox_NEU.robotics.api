package com.june.autonomy.action;

/**
 * Bewegung um eine Distanz; isBackward markiert negative Bewegungen.
 */
public class MoveAction implements IAction {
    private final int distance;
    private final boolean isBackward;

    public MoveAction(int distance, boolean isBackward) {
        this.distance = distance;
        this.isBackward = isBackward;
    }

    public int getDistance() {
        return distance;
    }

    public boolean isBackward() {
        return isBackward;
    }

    @Override
    public boolean isNegative() {
        return isBackward;
    }

    @Override
    public String getDescription() {
        return "Bewegung um " + distance + " Einheiten nach " + (isBackward ? "hinten" : "vorne");
    }
}
