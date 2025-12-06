package com.june.finance;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AutonomousResourceManagementAndFinanceSystem {
    private final Logger logger = Logger.getLogger(AutonomousResourceManagementAndFinanceSystem.class.getName());
    private final IResourceExtractionSystem resourceExtractionSystem;
    private final IBlockchainFinancialSystem blockchainFinancialSystem;

    public AutonomousResourceManagementAndFinanceSystem(IResourceExtractionSystem resourceExtractionSystem,
                                                        IBlockchainFinancialSystem blockchainFinancialSystem) {
        this.resourceExtractionSystem = resourceExtractionSystem;
        this.blockchainFinancialSystem = blockchainFinancialSystem;
    }

    public void operateSystem() {
        try {
            int extractedResources = resourceExtractionSystem.extractAndProcessResources();
            int requiredFunds = extractedResources * 10; // Beispielhafte Berechnung der benötigten Mittel
            blockchainFinancialSystem.allocateFundsForDevelopment(requiredFunds);
            logger.info("System erfolgreich betrieben.");
        } catch (ResourceExtractionException | FundingAllocationException e) {
            logger.log(Level.SEVERE, "Fehler im Systembetrieb: " + e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        IResourceExtractionSystem extractionSystem = new ResourceExtractionSystem("seltene Erden");
        IBlockchainFinancialSystem financialSystem = new BlockchainFinancialSystem();

        AutonomousResourceManagementAndFinanceSystem system = new AutonomousResourceManagementAndFinanceSystem(extractionSystem, financialSystem);
        system.operateSystem();
        system.operateSystem();
    }
}
