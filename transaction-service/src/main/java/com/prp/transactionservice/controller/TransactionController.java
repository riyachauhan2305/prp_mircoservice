package com.prp.transactionservice.controller;

import com.prp.transactionservice.model.Transaction;
import com.prp.transactionservice.service.TransactionService;
// import com.prp.authservice.service.AuthFlowService;
import com.prp.transactionservice.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

// import com.prp.authservice.model.ApiResponse;
import com.prp.transactionservice.model.ApiResponse;
// import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<ApiResponse> createTransaction(@RequestBody Transaction request,
                                                         @RequestHeader("Authorization") String authHeader) {
        String userId = jwtUtil.extractUserId(authHeader);
        Transaction saved = transactionService.createTransaction(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(201, "Transaction created successfully", saved));
    }

    // -------------------- GET ALL TRANSACTIONS -------------------- //
    // @GetMapping
    // public ResponseEntity<ApiResponse> getTransactions(HttpServletRequest httpRequest) {
    //     String userId = jwtUtil.extractUserId(httpRequest);
    //     List<Transaction> transactions = transactionService.getTransactionsByUser(userId);
    //     return ResponseEntity.ok(new ApiResponse(200, "Transactions fetched successfully", transactions));
    // }
  
    
}
