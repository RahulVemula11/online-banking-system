package com.rahul.banking.service;

import com.rahul.banking.dto.Dtos.*;
import com.rahul.banking.exception.ApiException;
import com.rahul.banking.model.Account;
import com.rahul.banking.model.User;
import com.rahul.banking.repository.AccountRepository;
import com.rahul.banking.repository.UserRepository;
import com.rahul.banking.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles registration (create user + account) and login (verify + issue token).
 */
@Service
public class AuthService {

    private final UserRepository userRepo;
    private final AccountRepository accountRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepo, AccountRepository accountRepo,
                       PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.accountRepo = accountRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepo.existsByUsername(req.username())) {
            throw new ApiException("Username already taken", 409);
        }

        // Hash the password before storing. The plain text never touches the DB.
        User user = new User(
                req.username(),
                passwordEncoder.encode(req.password()),
                req.fullName(),
                "ROLE_CUSTOMER"
        );
        userRepo.save(user);

        // Every new user gets an account with a generated number.
        String accountNumber = generateAccountNumber();
        Account account = new Account(accountNumber, user);
        accountRepo.save(account);

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getUsername(), user.getFullName(),
                user.getRole(), accountNumber);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepo.findByUsername(req.username())
                .orElseThrow(() -> new ApiException("Invalid username or password", 401));

        // matches() re-hashes the input and compares — constant-time, no plain text.
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new ApiException("Invalid username or password", 401);
        }

        Account account = accountRepo.findByUser_Username(user.getUsername())
                .orElseThrow(() -> new ApiException("Account not found", 404));

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getUsername(), user.getFullName(),
                user.getRole(), account.getAccountNumber());
    }

    private String generateAccountNumber() {
        // Simple unique-ish number for the demo: BANK + 8 digits.
        long n = (System.nanoTime() % 100_000_000L);
        return String.format("BANK%08d", n);
    }
}
