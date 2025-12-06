package com.june.controller;

import com.june.model.Transaction;
import com.june.model.Wallet;
import com.june.service.FinancialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/financial")
@CrossOrigin(origins = "*")
public class FinancialController {
    
    @Autowired
    private FinancialService financialService;
    
    /**
     * Create new wallet
     * POST /api/financial/wallets
     */
    @PostMapping("/wallets")
    public ResponseEntity<Wallet> createWallet(@RequestBody Wallet wallet) {
        try {
            Wallet created = financialService.createWallet(wallet);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    /**
     * Get all wallets
     * GET /api/financial/wallets
     */
    @GetMapping("/wallets")
    public ResponseEntity<List<Wallet>> getAllWallets() {
        List<Wallet> wallets = financialService.getAllWallets();
        return ResponseEntity.ok(wallets);
    }
    
    /**
     * Get wallet by ID
     * GET /api/financial/wallets/{id}
     */
    @GetMapping("/wallets/{id}")
    public ResponseEntity<Wallet> getWallet(@PathVariable Long id) {
        try {
            Wallet wallet = financialService.getWallet(id);
            return ResponseEntity.ok(wallet);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get wallets by type
     * GET /api/financial/wallets/type/{type}
     */
    @GetMapping("/wallets/type/{type}")
    public ResponseEntity<List<Wallet>> getWalletsByType(@PathVariable String type) {
        List<Wallet> wallets = financialService.getWalletsByType(type);
        return ResponseEntity.ok(wallets);
    }
    
    /**
     * Add transaction to wallet
     * POST /api/financial/wallets/{id}/transactions
     */
    @PostMapping("/wallets/{id}/transactions")
    public ResponseEntity<Transaction> addTransaction(
            @PathVariable Long id,
            @RequestBody Transaction transaction) {
        try {
            Transaction created = financialService.addTransaction(id, transaction);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    /**
     * Get all transactions for a wallet
     * GET /api/financial/wallets/{id}/transactions
     */
    @GetMapping("/wallets/{id}/transactions")
    public ResponseEntity<List<Transaction>> getWalletTransactions(@PathVariable Long id) {
        List<Transaction> transactions = financialService.getWalletTransactions(id);
        return ResponseEntity.ok(transactions);
    }
    
    /**
     * Transfer money between wallets
     * POST /api/financial/transfer
     */
    @PostMapping("/transfer")
    public ResponseEntity<String> transferMoney(@RequestBody Map<String, Object> transferData) {
        try {
            Long fromWalletId = Long.valueOf(transferData.get("fromWalletId").toString());
            Long toWalletId = Long.valueOf(transferData.get("toWalletId").toString());
            BigDecimal amount = new BigDecimal(transferData.get("amount").toString());
            String description = transferData.get("description").toString();
            
            financialService.transferBetweenWallets(fromWalletId, toWalletId, amount, description);
            return ResponseEntity.ok("Transfer successful");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Transfer failed: " + e.getMessage());
        }
    }
    
    /**
     * Get wallet income
     * GET /api/financial/wallets/{id}/income
     */
    @GetMapping("/wallets/{id}/income")
    public ResponseEntity<BigDecimal> getWalletIncome(@PathVariable Long id) {
        BigDecimal income = financialService.calculateWalletIncome(id);
        return ResponseEntity.ok(income);
    }
    
    /**
     * Get wallet expenses
     * GET /api/financial/wallets/{id}/expenses
     */
    @GetMapping("/wallets/{id}/expenses")
    public ResponseEntity<BigDecimal> getWalletExpenses(@PathVariable Long id) {
        BigDecimal expenses = financialService.calculateWalletExpenses(id);
        return ResponseEntity.ok(expenses);
    }
    
    /**
     * Get financial overview
     * GET /api/financial/overview
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getFinancialOverview() {
        Map<String, Object> overview = financialService.getFinancialOverview();
        return ResponseEntity.ok(overview);
    }
    
    /**
     * Get charity fund balance
     * GET /api/financial/charity-balance
     */
    @GetMapping("/charity-balance")
    public ResponseEntity<BigDecimal> getCharityBalance() {
        BigDecimal balance = financialService.getCharityFundBalance();
        return ResponseEntity.ok(balance);
    }
    
    /**
     * Delete wallet
     * DELETE /api/financial/wallets/{id}
     */
    @DeleteMapping("/wallets/{id}")
    public ResponseEntity<String> deleteWallet(@PathVariable Long id) {
        try {
            financialService.deleteWallet(id);
            return ResponseEntity.ok("Wallet deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Wallet not found");
        }
    }
}
