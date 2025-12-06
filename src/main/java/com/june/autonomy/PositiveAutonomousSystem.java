package com.june.autonomy;

/**
 * Demo-Entry-Point für ein positives autonomes System.
 * Führt Aktionen mit hoher und niedriger Aggressivität aus, wie in der Anforderung beschrieben.
 */
public class PositiveAutonomousSystem {
    public static void main(String[] args) {
        IAutonomousMachine machine = new AutonomousMachine();
        machine.setAggressiveness(0.8); // Setze eine höhere Aggressivität
        machine.performPositiveActions();
        machine.preventNegativeActions();
        machine.setAggressiveness(0.2); // Setze eine niedrigere Aggressivität
        machine.performPositiveActions();
        machine.preventNegativeActions();
    }
}
