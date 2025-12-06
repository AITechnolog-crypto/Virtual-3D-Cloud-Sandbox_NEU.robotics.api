package com.june.repository;

import com.june.model.Transaction;
import com.june.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByWallet(Wallet wallet);
    List<Transaction> findByWalletId(Long walletId);
    List<Transaction> findByTransactionType(String transactionType);
    List<Transaction> findByCategory(String category);
    List<Transaction> findByTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Transaction> findTop10ByOrderByTransactionDateDesc();
}
