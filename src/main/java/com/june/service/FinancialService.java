package com.june.service;

import com.june.model.Transaction;
import com.june.model.Wallet;
import com.june.repository.TransactionRepository;
import com.june.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FinancialService {
    
    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    /**
     * Create a new wallet
     */
    @Transactional
    public Wallet createWallet(Wallet wallet) {
        System.out.println("Creating new wallet: " + wallet.getWalletName());
        
        if (walletRepository.existsByWalletName(wallet.getWalletName())) {
            throw new RuntimeException("Wallet with name '" + wallet.getWalletName() + "' already exists");
        }
        
        return walletRepository.save(wallet);
    }
    
    /**
     * Get wallet by ID
     */
    public Wallet getWallet(Long id) {
        return walletRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Wallet not found with id: " + id));
    }
    
    /**
     * Get all wallets
     */
    public List<Wallet> getAllWallets() {
        return walletRepository.findAll();
    }
    
    /**
     * Get wallets by type
     */
    public List<Wallet> getWalletsByType(String type) {
        return walletRepository.findByWalletType(type);
    }
    
    /**
     * Add transaction to wallet
     */
    @Transactional
    public Transaction addTransaction(Long walletId, Transaction transaction) {
        System.out.println("Adding transaction to wallet " + walletId + ": " + 
                         transaction.getAmount() + " " + transaction.getTransactionType());
        
        Wallet wallet = getWallet(walletId);
        transaction.setWallet(wallet);
        
        // Update wallet balance
        BigDecimal amount = transaction.getAmount();
        switch (transaction.getTransactionType()) {
            case "INCOME":
                wallet.setBalance(wallet.getBalance().add(amount));
                break;
            case "EXPENSE":
                wallet.setBalance(wallet.getBalance().subtract(amount));
                break;
            case "CHARITY":
                wallet.setBalance(wallet.getBalance().subtract(amount));
                break;
            default:
                throw new RuntimeException("Unknown transaction type: " + transaction.getTransactionType());
        }
        
        walletRepository.save(wallet);
        return transactionRepository.save(transaction);
    }
    
    /**
     * Transfer money between wallets
     */
    @Transactional
    public void transferBetweenWallets(Long fromWalletId, Long toWalletId, 
                                        BigDecimal amount, String description) {
        System.out.println("Transferring " + amount + " from wallet " + fromWalletId + " to wallet " + toWalletId);
        
        Wallet fromWallet = getWallet(fromWalletId);
        Wallet toWallet = getWallet(toWalletId);
        
        if (fromWallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance in source wallet");
        }
        
        // Deduct from source wallet
        Transaction outgoingTransaction = new Transaction();
        outgoingTransaction.setWallet(fromWallet);
        outgoingTransaction.setTransactionType("TRANSFER");
        outgoingTransaction.setAmount(amount.negate());
        outgoingTransaction.setCategory("INTERNAL_TRANSFER");
        outgoingTransaction.setDescription("Transfer to " + toWallet.getWalletName() + ": " + description);
        outgoingTransaction.setTransactionDate(LocalDateTime.now());
        
        // Add to destination wallet
        Transaction incomingTransaction = new Transaction();
        incomingTransaction.setWallet(toWallet);
        incomingTransaction.setTransactionType("TRANSFER");
        incomingTransaction.setAmount(amount);
        incomingTransaction.setCategory("INTERNAL_TRANSFER");
        incomingTransaction.setDescription("Transfer from " + fromWallet.getWalletName() + ": " + description);
        incomingTransaction.setTransactionDate(LocalDateTime.now());
        
        // Update balances
        fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
        toWallet.setBalance(toWallet.getBalance().add(amount));
        
        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);
        transactionRepository.save(outgoingTransaction);
        transactionRepository.save(incomingTransaction);
    }
    
    /**
     * Get all transactions for a wallet
     */
    public List<Transaction> getWalletTransactions(Long walletId) {
        return transactionRepository.findByWalletId(walletId);
    }
    
    /**
     * Get transactions within date range
     */
    public List<Transaction> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByTransactionDateBetween(startDate, endDate);
    }
    
    /**
     * Calculate total income for wallet
     */
    public BigDecimal calculateWalletIncome(Long walletId) {
        BigDecimal income = transactionRepository.calculateTotalIncome(walletId);
        return income != null ? income : BigDecimal.ZERO;
    }
    
    /**
     * Calculate total expenses for wallet
     */
    public BigDecimal calculateWalletExpenses(Long walletId) {
        BigDecimal expenses = transactionRepository.calculateTotalExpenses(walletId);
        return expenses != null ? expenses : BigDecimal.ZERO;
    }
    
    /**
     * Get financial overview
     */
    public Map<String, Object> getFinancialOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        List<Wallet> allWallets = walletRepository.findAll();
        BigDecimal totalBalance = allWallets.stream()
            .filter(w -> w.getCurrency().equals("EUR"))
            .map(Wallet::getBalance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        overview.put("totalBalance", totalBalance);
        overview.put("walletsCount", allWallets.size());
        overview.put("wallets", allWallets);
        
        List<Object[]> walletStats = walletRepository.getWalletStatistics();
        overview.put("walletStatistics", walletStats);
        
        List<Object[]> transactionStats = transactionRepository.getTransactionStatistics();
        overview.put("transactionStatistics", transactionStats);
        
        LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1);
        List<Transaction> recentTransactions = transactionRepository.findRecentTransactions(lastMonth);
        overview.put("recentTransactions", recentTransactions);
        
        return overview;
    }
    
    /**
     * Get charity fund balance
     */
    public BigDecimal getCharityFundBalance() {
        List<Wallet> charityWallets = walletRepository.findByWalletType("CHARITY");
        return charityWallets.stream()
            .map(Wallet::getBalance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Delete wallet
     */
    @Transactional
    public void deleteWallet(Long id) {
        System.out.println("Deleting wallet with id: " + id);
        walletRepository.deleteById(id);
    }
}
