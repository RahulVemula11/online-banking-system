package com.rahul.banking.config;

import com.rahul.banking.model.Account;
import com.rahul.banking.model.User;
import com.rahul.banking.repository.AccountRepository;
import com.rahul.banking.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

/**
 * Seeds two demo users the first time the app starts, so anyone visiting the
 * live demo can log in immediately without registering. Runs only if the DB is empty.
 *
 * Demo logins:
 *   alice / password123   (starts with 5000.00)
 *   bob   / password123   (starts with 1500.00)
 *   admin / admin123       (ROLE_ADMIN)
 */
@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seed(UserRepository userRepo,
                                  AccountRepository accountRepo,
                                  PasswordEncoder encoder) {
        return args -> {
            if (userRepo.count() > 0) return; // already seeded

            createUser(userRepo, accountRepo, encoder,
                    "alice", "password123", "Alice Demo", "ROLE_CUSTOMER",
                    "BANK10000001", new BigDecimal("5000.00"));

            createUser(userRepo, accountRepo, encoder,
                    "bob", "password123", "Bob Demo", "ROLE_CUSTOMER",
                    "BANK10000002", new BigDecimal("1500.00"));

            createUser(userRepo, accountRepo, encoder,
                    "admin", "admin123", "Admin User", "ROLE_ADMIN",
                    "BANK10000003", new BigDecimal("0.00"));
        };
    }

    private void createUser(UserRepository userRepo, AccountRepository accountRepo,
                            PasswordEncoder encoder, String username, String rawPass,
                            String fullName, String role, String accountNumber,
                            BigDecimal startingBalance) {
        User user = new User(username, encoder.encode(rawPass), fullName, role);
        userRepo.save(user);
        Account acc = new Account(accountNumber, user);
        acc.setBalance(startingBalance);
        accountRepo.save(acc);
    }
}
