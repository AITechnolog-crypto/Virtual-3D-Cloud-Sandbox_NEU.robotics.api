package com.june.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.june.repository.TransactionRepository;
import com.june.model.Transaction;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {
    @Autowired
    private TransactionRepository transactionRepository;

    @GetMapping
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @GetMapping("/count")
    public long countTransactions() {
        return transactionRepository.count();
    }

    @GetMapping("/recent")
    public List<Transaction> getRecentTransactions() {
        return transactionRepository.findTop10ByOrderByCreatedAtDesc();
    }
}
