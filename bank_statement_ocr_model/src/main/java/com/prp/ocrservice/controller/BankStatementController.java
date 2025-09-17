package com.prp.ocrservice.controller;

import com.prp.ocrservice.model.BankTransaction;
import com.prp.ocrservice.service.BankStatementParser;
import com.prp.ocrservice.service.SuryaOcrService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bank-statement")
public class BankStatementController {

    @Autowired
    private SuryaOcrService ocrService;

    @Autowired
    private BankStatementParser parser;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadBankStatement(@RequestParam("file") MultipartFile file) {
        System.out.println(" Upload API called!");
        System.out.println("File received: " + file.getOriginalFilename());
        
        try {
            String ocrText = ocrService.extractText(file);
            System.out.println("Extracted OCR Text:\n" + ocrText);

            List<BankTransaction> transactions = parser.parse(ocrText);
            System.out.println("Transactions parsed: " + transactions.size());

            return ResponseEntity.ok(Map.of("status", "success", "data", transactions));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}

