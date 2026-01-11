package com.easypay.api.service;

import com.easypay.api.dto.TransferDTO;
import com.easypay.api.model.Transaction;
import com.easypay.api.model.User;
import com.easypay.api.model.Wallet;
import com.easypay.api.model.enums.TransactionStatus;
import com.easypay.api.model.enums.UserType;
import com.easypay.api.repository.TransactionRepository;
import com.easypay.api.repository.UserRepository;
import com.easypay.api.repository.WalletRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository, WalletRepository walletRepository, PasswordEncoder passwordEncoder) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Performs a money transfer between two users.
     * This method is transactional: if any step fails, the entire operation is rolled back,
     * ensuring that money is never lost or created out of thin air.
     *
     * @param transferDTO Data Transfer Object containing transaction details.
     * @return The created Transaction entity.
     */
    @Transactional
    public Transaction createTransaction(TransferDTO transferDTO) {
        String senderDocument = transferDTO.senderDocument();
        String receiverDocument = transferDTO.receiverDocument();
        BigDecimal amount = transferDTO.amount();
        String requestPassword = transferDTO.senderPassword();

        // Idempotency Strategy: If the client doesn't send a key, we generate one to ensure DB consistency.
        // If the key repeats, the Database Unique Constraint will throw an exception, preventing duplicates.
        String idempotencyKey = transferDTO.idempotencyKey() == null
                ? UUID.randomUUID().toString()
                : transferDTO.idempotencyKey();

        User sender = userRepository.findByDocument(senderDocument).
                orElseThrow(() -> new RuntimeException("Sender " + senderDocument + " not found"));

        User receiver = userRepository.findByDocument(receiverDocument).
                orElseThrow(() -> new RuntimeException("Receiver " + receiverDocument + " not found"));

        // 1. Password validation
        boolean passwordValid = passwordEncoder.matches(requestPassword, sender.getPassword());
        if (!passwordValid) {
            throw new RuntimeException("Password incorrect");
        }

        // 2. Business rules validations
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        if (senderDocument.equals(receiverDocument)) {
            throw new IllegalArgumentException("Cannot transfer to yourself");
        }

        if (sender.getUserType() == UserType.MERCHANT) {
            throw new RuntimeException("Merchants cannot send transfers");
        }

        Wallet senderWallet = walletRepository.findByUser_Id(sender.getId())
                .orElseThrow(() -> new RuntimeException(
                        "Wallet not found for user " + senderDocument
                ));
        Wallet receiverWallet = walletRepository.findByUser_Id(receiver.getId())
                .orElseThrow(() -> new RuntimeException(
                        "Wallet not found for user " + receiverDocument
                ));

        BigDecimal senderBalance = senderWallet.getBalance();
        BigDecimal receiverBalance = receiverWallet.getBalance();
        if (senderBalance.compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // TODO:
        // Call External Authorization Service (Mock)
        // Check if transaction is authorized by external API. Throw Exception if not.

        // TODO:
        // In high-concurrency scenarios, protect wallet reads/updates
        // with pessimistic locking to prevent double spending.

        // Transaction
        senderWallet.setBalance(senderBalance.subtract(amount));
        receiverWallet.setBalance(receiverBalance.add(amount));

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        Transaction transaction = new Transaction();
        transaction.setSenderUser(sender);
        transaction.setReceiverUser(receiver);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setIdempotencyKey(idempotencyKey);

        return transactionRepository.save(transaction);
    }
}
