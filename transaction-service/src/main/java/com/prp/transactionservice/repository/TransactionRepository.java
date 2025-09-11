package com.prp.transactionservice.repository;

import com.prp.transactionservice.model.Transaction;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends CrudRepository<Transaction, String> {

    Transaction save(Transaction txRequest);
    
    List<Transaction> findByUserId(String userId);
}
