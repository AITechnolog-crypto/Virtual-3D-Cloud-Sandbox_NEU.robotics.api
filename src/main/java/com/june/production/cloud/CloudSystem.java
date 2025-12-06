package com.june.production.cloud;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Orchestrator für Cloudbetrieb: allokiert Ressourcen, integriert Technologie, startet.
 */
public class CloudSystem {
    private static final Logger logger = Logger.getLogger(CloudSystem.class.getName());
    private final ICloudResourceManager resourceManager;
    private final ITechnologyIntegrator technologyIntegrator;

    public CloudSystem(ICloudResourceManager resourceManager, ITechnologyIntegrator technologyIntegrator) {
        this.resourceManager = resourceManager;
        this.technologyIntegrator = technologyIntegrator;
    }

    public void operateSystem() {
        try {
            resourceManager.allocateResources();
            technologyIntegrator.integrateTechnology();
            logger.info("Cloud-System erfolgreich gestartet.");
        } catch (ResourceAllocationException | IntegrationFailedException e) {
            logger.log(Level.SEVERE, "Fehler im Cloud-Systembetrieb: " + e.getMessage(), e);
        }
    }

    /** Öffentliche Hilfsmethode entsprechend der Issue-Vorlage. */
    public void deallocateAll() {
        resourceManager.deallocateResources();
    }

    // Demo main
    public static void main(String[] args) {
        ICloudResourceManager resourceManager = new CloudResourceManager();
        ITechnologyIntegrator technologyIntegrator = new TechnologyIntegrator();
        CloudSystem cloudSystem = new CloudSystem(resourceManager, technologyIntegrator);
        cloudSystem.operateSystem();
        cloudSystem.operateSystem();
        cloudSystem.deallocateAll();
    }
}