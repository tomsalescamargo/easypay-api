package com.easypay.api.controller;


import com.easypay.api.dto.TransferDTO;
import com.easypay.api.model.Transaction;
import com.easypay.api.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(
            @Valid @RequestBody TransferDTO transferDTO
    ) {
        Transaction savedTransaction = service.createTransaction(transferDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedTransaction);
    }
}
