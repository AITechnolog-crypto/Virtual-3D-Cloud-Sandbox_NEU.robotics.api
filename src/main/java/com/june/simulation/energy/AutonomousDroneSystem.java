package com.june.simulation.energy;

/**
 * Abstraktes Basissystem für Drohnen, das positive Einsätze ermöglicht.
 */
public abstract class AutonomousDroneSystem {
    protected final IResourceManagement resourceManagement;

    protected AutonomousDroneSystem(IResourceManagement resourceManagement) {
        this.resourceManagement = resourceManagement;
    }

    public void deployDronesForGood() {
        System.out.println("Drohnen werden für positive Zwecke eingesetzt.");
        if (resourceManagement != null) {
            resourceManagement.manageResources();
        }
    }
}
