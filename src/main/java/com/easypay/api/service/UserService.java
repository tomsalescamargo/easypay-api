package com.easypay.api.service;

import com.easypay.api.dto.CreateUserDTO;
import com.easypay.api.model.User;
import com.easypay.api.model.Wallet;
import com.easypay.api.repository.UserRepository;
import com.easypay.api.repository.WalletRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, WalletRepository walletRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(CreateUserDTO userDTO) {
        Optional<User> existingUserByDocument = userRepository.findByDocument(userDTO.document());
        Optional<User> existingUserByEmail = userRepository.findByEmail(userDTO.email());
        if (existingUserByDocument.isPresent() || existingUserByEmail.isPresent()) {
            throw new RuntimeException("User already exists");
        }

        User user = new User();
        user.setName(userDTO.name());
        user.setDocument(userDTO.document());
        user.setEmail(userDTO.email());
        user.setPassword(passwordEncoder.encode(userDTO.password()));
        user.setUserType(userDTO.userType());
        userRepository.save(user);

        Wallet wallet = new Wallet(user, new BigDecimal("100.0"));
        walletRepository.save(wallet);

        return user;
    }
}
