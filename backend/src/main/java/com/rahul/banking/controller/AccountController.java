package com.rahul.banking.controller;

import com.rahul.banking.dto.Dtos.*;
import com.rahul.banking.model.Account;
import com.rahul.banking.model.Transaction;
import com.rahul.banking.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Protected endpoints — every method here requires a valid JWT.
 * The logged-in username comes from the token (via Authentication), NOT from
 * the request body. That means a user can only ever act on their OWN account —
 * they can't pass someone else's account number and move that person's money.
 */
@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public AccountResponse myAccount(Authentication auth) {
        Account acc = accountService.getByUsername(auth.getName());
        return toAccountResponse(acc);
    }

    @PostMapping("/deposit")
    public AccountResponse deposit(Authentication auth, @Valid @RequestBody AmountRequest req) {
        Account acc = accountService.getByUsername(auth.getName());
        Account updated = accountService.deposit(acc.getAccountNumber(), req.amount());
        return toAccountResponse(updated);
    }

    @PostMapping("/withdraw")
    public AccountResponse withdraw(Authentication auth, @Valid @RequestBody AmountRequest req) {
        Account acc = accountService.getByUsername(auth.getName());
        Account updated = accountService.withdraw(acc.getAccountNumber(), req.amount());
        return toAccountResponse(updated);
    }

    @PostMapping("/transfer")
    public AccountResponse transfer(Authentication auth, @Valid @RequestBody TransferRequest req) {
        Account acc = accountService.getByUsername(auth.getName());
        Account updated = accountService.transfer(acc.getAccountNumber(), req.toAccount(), req.amount());
        return toAccountResponse(updated);
    }

    @GetMapping("/statement")
    public StatementResponse statement(Authentication auth) {
        Account acc = accountService.getByUsername(auth.getName());
        List<Transaction> txns = accountService.history(acc.getAccountNumber());
        List<TransactionResponse> mapped = txns.stream()
                .map(t -> new TransactionResponse(
                        t.getType(), t.getAmount(), t.getCounterpartyAccount(),
                        t.getBalanceAfter(), t.getDescription(), t.getTimestamp()))
                .toList();
        return new StatementResponse(toAccountResponse(acc), mapped);
    }

    private AccountResponse toAccountResponse(Account acc) {
        return new AccountResponse(acc.getAccountNumber(), acc.getBalance(),
                acc.getUser().getFullName());
    }
}
