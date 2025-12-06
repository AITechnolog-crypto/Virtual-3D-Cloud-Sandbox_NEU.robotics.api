package com.june.factory;

/**
 * Exception für Blockchain-Transaktionen.
 */
public class BlockchainTransactionException extends Exception {
    public BlockchainTransactionException(String message) {
        super(message);
    }
}
