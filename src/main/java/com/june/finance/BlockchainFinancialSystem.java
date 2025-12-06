package com.june.finance;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BlockchainFinancialSystem implements IBlockchainFinancialSystem {
    private final Logger logger = Logger.getLogger(BlockchainFinancialSystem.class.getName());
    private int availableFunds = 1000; // Startkapital

    @Override
    public void allocateFundsForDevelopment(int requiredFunds) throws FundingAllocationException {
        try {
            if (availableFunds >= requiredFunds) {
                availableFunds -= requiredFunds;
                logger.info(requiredFunds + " Mittel wurden für die Entwicklung zugewiesen. Verfügbare Mittel: " + availableFunds);
            } else {
                throw new FundingAllocationException("Nicht genügend Mittel verfügbar.", null);
            }
        } catch (FundingAllocationException e) {
            logger.log(Level.SEVERE, "Fehler bei der Mittelzuweisung", e);
            throw e;
        }
    }
}
