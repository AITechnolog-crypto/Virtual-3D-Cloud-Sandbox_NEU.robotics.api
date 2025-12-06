package com.june.production;

import java.util.logging.Logger;

/**
 * Sichere Blockchain-Wallet-Attrappe mit einfachem Kontostand.
 */
public class SecureBlockchainWallet implements IBlockchainWallet {
    private static final Logger logger = Logger.getLogger(SecureBlockchainWallet.class.getName());

    private double balance;

    public SecureBlockchainWallet() {
        this(10_000.0); // Default-Budget
    }

    public SecureBlockchainWallet(double initialBalance) {
        this.balance = initialBalance;
    }

    @Override
    public double getBalance() {
        return balance;
    }

    @Override
    public void processTransaction(Transaction transaction) {
        logger.info("Blockchain Transaktion durchgeführt: " + transaction);
        // Beispiel: Ziehe symbolisch einen kleinen Betrag ab
        this.balance = Math.max(0.0, this.balance - 100.0);
    }
}