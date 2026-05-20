package com.rahul.banking.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * A Transaction is a permanent record of one money movement.
 * We never delete or edit these — they're the audit log. A bank must be able
 * to prove what happened, so every deposit, withdrawal, and transfer leaves a row here.
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT
    @Column(nullable = false)
    private String type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    // The account this row belongs to (whose statement it shows up on)
    @Column(nullable = false)
    private String accountNumber;

    // For transfers: the other side of the movement. Null for deposits/withdrawals.
    private String counterpartyAccount;

    // Running balance of this account right after the transaction
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    private String description;

    @Column(nullable = false, updatable = false)
    private Instant timestamp = Instant.now();

    public Transaction() { }

    public Transaction(String type, BigDecimal amount, String accountNumber,
                       String counterpartyAccount, BigDecimal balanceAfter, String description) {
        this.type = type;
        this.amount = amount;
        this.accountNumber = accountNumber;
        this.counterpartyAccount = counterpartyAccount;
        this.balanceAfter = balanceAfter;
        this.description = description;
    }

    // --- getters and setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getCounterpartyAccount() { return counterpartyAccount; }
    public void setCounterpartyAccount(String counterpartyAccount) { this.counterpartyAccount = counterpartyAccount; }

    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
