package com.georgii.onlinebankingsystem.service;

import com.georgii.onlinebankingsystem.dto.auth.AuthResponse;
import com.georgii.onlinebankingsystem.dto.auth.RegisterRequest;
import com.georgii.onlinebankingsystem.entity.Account;
import com.georgii.onlinebankingsystem.entity.User;
import com.georgii.onlinebankingsystem.repository.AccountRepository;
import com.georgii.onlinebankingsystem.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            UserRepository userRepository,
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        String email = normalizeEmail(request.getEmail());
        String rawPassword = request.getPassword();

        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password must not be blank");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("User with this email already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);

        Account account = new Account();
        account.setUser(user);
        account.setAccountNumber(generateAccountNumber());
        accountRepository.save(account);

        return new AuthResponse(user.getId(), user.getEmail(), account.getAccountNumber());
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be blank");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String generateAccountNumber() {
        String accountNumber;
        do {
            accountNumber = randomDigits(12);
        } while (accountRepository.findByAccountNumber(accountNumber).isPresent());
        return accountNumber;
    }

    private String randomDigits(int length) {
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            result.append(secureRandom.nextInt(10));
        }
        return result.toString();
    }
}

