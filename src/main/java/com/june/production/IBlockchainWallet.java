package com.june.production;

/**
 * Blockchain-Wallet Schnittstelle für Produktions- und Transaktionsflüsse.
 */
public interface IBlockchainWallet {
    /**
     * Liefert den aktuellen Kontostand (Budget) in Währungseinheiten.
     */
    double getBalance();

    /**
     * Verarbeitet eine Transaktion (z. B. Produktion/Investition).
     */
    void processTransaction(Transaction transaction);
}