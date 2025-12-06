package com.june.production;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Orchestriert Wallet, Ressourcenbeschaffung und Produktionsstart.
 */
public class AIProductionManager {
    private static final Logger logger = Logger.getLogger(AIProductionManager.class.getName());

    private final IBlockchainWallet blockchainWallet;
    private final IResourceAcquisition resourceAcquisition;
    private final IProductionControl productionControl;

    public AIProductionManager(IBlockchainWallet blockchainWallet,
                               IResourceAcquisition resourceAcquisition,
                               IProductionControl productionControl) {
        this.blockchainWallet = blockchainWallet;
        this.resourceAcquisition = resourceAcquisition;
        this.productionControl = productionControl;
    }

    public void manageProduction() {
        try {
            double budget = blockchainWallet.getBalance();
            if (budget < 0) {
                throw new InsufficientFundsException("Negativer Kontostand.");
            }
            resourceAcquisition.acquireResources(budget);
            productionControl.startProduction();
            logger.info("Produktion erfolgreich abgeschlossen.");
        } catch (InsufficientFundsException | ResourceAcquisitionException | ProductionStartException e) {
            logger.log(Level.SEVERE, "Fehler im Produktionsmanagement: " + e.getMessage(), e);
        }
    }

    // Kleiner Demo-Entry-Point
    public static void main(String[] args) {
        AIProductionManager mgr = new AIProductionManager(
                new SecureBlockchainWallet(6_000.0),
                new ResourceAcquisitionImplementation(),
                new ProductionControlImplementation()
        );
        mgr.manageProduction();
    }
}