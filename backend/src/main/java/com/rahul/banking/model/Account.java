package com.rahul.banking.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * An Account holds money. It belongs to exactly one User.
 *
 * IMPORTANT: balance is BigDecimal, NOT double.
 * Money must never be stored as a floating-point number — 0.1 + 0.2 != 0.3
 * in floating point, and "losing a cent" in a bank is a real bug. BigDecimal
 * is exact. This is a detail interviewers love to check.
 */
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // A human-friendly account number, e.g. "BANK0001"
    @Column(nullable = false, unique = true)
    private String accountNumber;

    // Exact decimal balance. Starts at zero.
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    // The owner. One account <-> one user.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // @Version enables optimistic locking: if two transfers touch the same
    // account at the same time, the second one fails instead of silently
    // corrupting the balance. This is how we keep money safe under concurrency.
    @Version
    private Long version;

    public Account() { }

    public Account(String accountNumber, User user) {
        this.accountNumber = accountNumber;
        this.user = user;
        this.balance = BigDecimal.ZERO;
    }

    // --- getters and setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
