package com.june.factory;

import java.util.logging.Logger;

/**
 * Beispiel-Implementierung einer Blockchain-Wallet.
 */
public class BlockchainWallet implements IBlockchainWallet {
    private final Logger logger = Logger.getLogger(BlockchainWallet.class.getName());

    @Override
    public void processTransaction(Transaction transaction) {
        logger.info("Blockchain Transaktion durchgeführt: " + transaction);
        // In echter Implementierung: Signieren/Senden/Bestätigen
    }
}
