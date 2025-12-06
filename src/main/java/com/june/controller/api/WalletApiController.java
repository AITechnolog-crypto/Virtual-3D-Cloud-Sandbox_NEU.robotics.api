package com.june.controller.api.v1.simulation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.june.repository.WalletRepository;
import com.june.model.Wallet;
import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@CrossOrigin(origins = "*")
public class WalletApiController {
    
    @Autowired
    private WalletRepository walletRepository;
    
    @GetMapping
    public List<Wallet> getAllWallets() {
        return walletRepository.findAll();
    }
    
    @GetMapping("/count")
    public long countWallets() {
        return walletRepository.count();
    }
    
    @GetMapping("/total-balance")
    public double getTotalBalance() {
        return walletRepository.findAll().stream()
            .mapToDouble(w -> w.getBalance().doubleValue())
            .sum();
    }
}
