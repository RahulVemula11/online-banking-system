package com.rahul.banking.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * DTOs (Data Transfer Objects) are the shapes the API sends and receives.
 * We keep them SEPARATE from the database entities so we never accidentally
 * expose a password hash or internal field to the outside world.
 */
public class Dtos {

    // ----- requests coming IN -----

    public record RegisterRequest(
            @NotBlank String username,
            @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String password,
            @NotBlank String fullName
    ) {}

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {}

    public record AmountRequest(
            @NotNull @DecimalMin(value = "0.01", message = "Amount must be positive") BigDecimal amount
    ) {}

    public record TransferRequest(
            @NotBlank String toAccount,
            @NotNull @DecimalMin(value = "0.01", message = "Amount must be positive") BigDecimal amount
    ) {}

    // ----- responses going OUT -----

    public record AuthResponse(String token, String username, String fullName,
                               String role, String accountNumber) {}

    public record AccountResponse(String accountNumber, BigDecimal balance,
                                  String ownerName) {}

    public record TransactionResponse(String type, BigDecimal amount, String counterparty,
                                      BigDecimal balanceAfter, String description, Instant timestamp) {}

    public record StatementResponse(AccountResponse account, List<TransactionResponse> transactions) {}

    public record MessageResponse(String message) {}
}
