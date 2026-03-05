package com.easypay.api;

import com.easypay.api.dto.TransferDTO;
import com.easypay.api.model.Transaction;
import com.easypay.api.model.User;
import com.easypay.api.model.Wallet;
import com.easypay.api.model.enums.UserType;
import com.easypay.api.repository.TransactionRepository;
import com.easypay.api.repository.UserRepository;
import com.easypay.api.repository.WalletRepository;
import com.easypay.api.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    @DisplayName("Should save a new transaction")
    void shouldCreateTransaction() {
        // --- Arrange ---
        String senderDocument = "12345678910";
        String receiverDocument = "01651352957";
        String rawSenderPassword = "lovespring";
        BigDecimal amount = new BigDecimal("25.0");
        TransferDTO transferDTO = new TransferDTO(
                senderDocument, receiverDocument, amount, rawSenderPassword, null
        );

        UUID senderId = UUID.randomUUID();
        String senderPassword = "encoded-sender-password";
        User senderUser = new User(senderId, "Tom", "tom@gmail.com", senderPassword,
                senderDocument, UserType.COMMON, LocalDateTime.now());

        Wallet senderWallet = new Wallet(senderUser, new BigDecimal("100.0"));

        UUID receiverId = UUID.randomUUID();
        String receiverPassword = "encoded-receiver-password";
        User receiverUser = new User(receiverId, "Pedro", "pedro@gmail.com", receiverPassword,
                receiverDocument, UserType.COMMON, LocalDateTime.now());

        Wallet receiverWallet = new Wallet(receiverUser, new BigDecimal("100.0"));

        when(userRepository.findByDocument(senderDocument)).thenReturn(Optional.of(senderUser));
        when(userRepository.findByDocument(receiverDocument)).thenReturn(Optional.of(receiverUser));

        when(passwordEncoder.matches(rawSenderPassword, senderPassword)).thenReturn(true);

        when(walletRepository.findByUser_Id(senderId)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByUser_Id(receiverId)).thenReturn(Optional.of(receiverWallet));

        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArguments()[0]);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        // --- Act ---
        Transaction result = transactionService.createTransaction(transferDTO);

        // --- Assert ---
        assertNotNull(result);
        assertEquals(new BigDecimal("75.0"), senderWallet.getBalance());
        assertEquals(new BigDecimal("125.0"), receiverWallet.getBalance());

        verify(walletRepository, times(2)).save(any(Wallet.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should block transaction if sender has insufficient balance")
    void shouldBlockInsufficientBalance() {
        // --- Arrange ---
        String senderDocument = "12345678910";
        String receiverDocument = "01651352957";
        String rawSenderPassword = "lovespring";
        BigDecimal amount = new BigDecimal("115.8");
        TransferDTO transferDTO = new TransferDTO(
                senderDocument, receiverDocument, amount, rawSenderPassword, null
        );

        UUID senderId = UUID.randomUUID();
        String senderPassword = "encoded-sender-password";
        User senderUser = new User(senderId, "Tom", "tom@gmail.com", senderPassword,
                senderDocument, UserType.COMMON, LocalDateTime.now());

        Wallet senderWallet = new Wallet(senderUser, new BigDecimal("100.0"));

        UUID receiverId = UUID.randomUUID();
        String receiverPassword = "encoded-receiver-password";
        User receiverUser = new User(receiverId, "Pedro", "pedro@gmail.com", receiverPassword,
                receiverDocument, UserType.COMMON, LocalDateTime.now());

        Wallet receiverWallet = new Wallet(receiverUser, new BigDecimal("100.0"));

        when(userRepository.findByDocument(senderDocument)).thenReturn(Optional.of(senderUser));
        when(userRepository.findByDocument(receiverDocument)).thenReturn(Optional.of(receiverUser));

        when(passwordEncoder.matches(rawSenderPassword, senderPassword)).thenReturn(true);

        when(walletRepository.findByUser_Id(senderId)).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByUser_Id(receiverId)).thenReturn(Optional.of(receiverWallet));

        // --- Act & Assert ---
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> transactionService.createTransaction(transferDTO));

        assertEquals("Insufficient balance", exception.getMessage());

        // Verifying that save methods were never called (rollback behavior)
        verify(walletRepository, times(0)).save(any());
        verify(transactionRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("Should block transaction if sender is a Merchant")
    void shouldBlockMerchantTransaction() {
        // --- Arrange ---
        String senderDocument = "12345678911";
        String receiverDocument = "01651352957";
        String rawSenderPassword = "lovejava";
        BigDecimal amount = new BigDecimal("10.0");
        TransferDTO transferDTO = new TransferDTO(
                senderDocument, receiverDocument, amount, rawSenderPassword, null
        );

        UUID senderId = UUID.randomUUID();
        String senderPassword = "encoded-sender-password";

        // Sender is a Merchant
        User senderUser = new User(senderId, "Rodrigo", "rodrigo@gmail.com", senderPassword,
                senderDocument, UserType.MERCHANT, LocalDateTime.now());

        Wallet senderWallet = new Wallet(senderUser, new BigDecimal("100.0"));

        UUID receiverId = UUID.randomUUID();
        String receiverPassword = "encoded-receiver-password";
        User receiverUser = new User(receiverId, "Pedro", "pedro@gmail.com", receiverPassword,
                receiverDocument, UserType.COMMON, LocalDateTime.now());

        Wallet receiverWallet = new Wallet(receiverUser, new BigDecimal("100.0"));

        when(userRepository.findByDocument(senderDocument)).thenReturn(Optional.of(senderUser));
        when(userRepository.findByDocument(receiverDocument)).thenReturn(Optional.of(receiverUser));

        when(passwordEncoder.matches(rawSenderPassword, senderPassword)).thenReturn(true);

        // --- Act & Assert ---
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> transactionService.createTransaction(transferDTO));

        assertEquals("Merchants cannot send transfers", exception.getMessage());

        verify(walletRepository, times(0)).save(any());
        verify(transactionRepository, times(0)).save(any());
    }
}