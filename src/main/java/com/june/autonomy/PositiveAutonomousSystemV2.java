package com.june.autonomy;

import com.june.autonomy.action.CollectResourceAction;
import com.june.autonomy.action.IAction;
import com.june.autonomy.action.MoveAction;

/**
 * Hauptklasse für das autonome System (angepasst für IAction-Workflow).
 */
public class PositiveAutonomousSystemV2 {
    public static void main(String[] args) {
        IAutonomousMachine machine = new AutonomousMachine();

        IAction moveForward = new MoveAction(10, false);
        IAction moveBackward = new MoveAction(5, true);
        IAction collectGold = new CollectResourceAction("Gold");

        machine.performPositiveActions(moveForward);  // Ausgeführt
        machine.performPositiveActions(moveBackward); // Nicht ausgeführt (negativ)
        machine.performPositiveActions(collectGold);  // Ausgeführt
    }
}
