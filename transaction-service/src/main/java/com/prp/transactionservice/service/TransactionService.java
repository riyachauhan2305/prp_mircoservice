package com.prp.transactionservice.service;

import com.prp.transactionservice.model.Transaction;
import com.prp.transactionservice.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    // -------------------- CREATE TRANSACTION -------------------- //
    public Transaction createTransaction(String userId, Transaction txRequest) {
        txRequest.setUserId(userId);
        return transactionRepository.save(txRequest);
    }

    public List<Transaction> getTransactionsByUser(String userId) {
        return transactionRepository.findByUserId(userId);
    }
}
