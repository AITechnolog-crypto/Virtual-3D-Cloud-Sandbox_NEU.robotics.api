package com.june.repository;

import com.june.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    
    Optional<Wallet> findByWalletName(String walletName);
    List<Wallet> findByWalletType(String walletType);
    List<Wallet> findByCurrency(String currency);
    List<Wallet> findByBalanceGreaterThan(BigDecimal amount);
    
    @Query("SELECT SUM(w.balance) FROM Wallet w WHERE w.currency = :currency")
    BigDecimal getTotalBalanceByCurrency(String currency);
    
    @Query("SELECT w.walletType, SUM(w.balance), COUNT(w) FROM Wallet w GROUP BY w.walletType")
    List<Object[]> getWalletStatistics();
    
    boolean existsByWalletName(String walletName);
}
