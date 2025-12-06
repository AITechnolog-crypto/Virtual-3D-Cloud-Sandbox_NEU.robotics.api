package com.june.factory;

/**
 * Schnittstelle für Blockchain-Wallet.
 */
public interface IBlockchainWallet {
    void processTransaction(Transaction transaction) throws BlockchainTransactionException;
}
