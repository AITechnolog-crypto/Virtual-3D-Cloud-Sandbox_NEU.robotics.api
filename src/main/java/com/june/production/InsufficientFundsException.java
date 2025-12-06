package com.june.production;

/**
 * Wird geworfen, wenn das Wallet einen negativen oder unzureichenden Kontostand meldet.
 */
public class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String message) {
        super(message);
    }
}