package com.rahul.banking.service;

import com.rahul.banking.exception.ApiException;
import com.rahul.banking.model.Account;
import com.rahul.banking.model.Transaction;
import com.rahul.banking.repository.AccountRepository;
import com.rahul.banking.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * The SERVICE layer holds the business rules — the actual "banking".
 * Controllers should be thin and just call into here. This is where money moves.
 */
@Service
public class AccountService {

    private final AccountRepository accountRepo;
    private final TransactionRepository txnRepo;

    // Spring hands us the repositories automatically (dependency injection).
    public AccountService(AccountRepository accountRepo, TransactionRepository txnRepo) {
        this.accountRepo = accountRepo;
        this.txnRepo = txnRepo;
    }

    public Account getByUsername(String username) {
        return accountRepo.findByUser_Username(username)
                .orElseThrow(() -> new ApiException("Account not found", 404));
    }

    public List<Transaction> history(String accountNumber) {
        return txnRepo.findByAccountNumberOrderByTimestampDesc(accountNumber);
    }

    /**
     * DEPOSIT money into an account.
     * @Transactional means: everything inside this method either fully succeeds
     * or fully rolls back. No half-finished states.
     */
    @Transactional
    public Account deposit(String accountNumber, BigDecimal amount) {
        requirePositive(amount);
        Account acc = accountRepo.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new ApiException("Account not found", 404));

        acc.setBalance(acc.getBalance().add(amount));
        accountRepo.save(acc);

        record("DEPOSIT", amount, acc.getAccountNumber(), null, acc.getBalance(), "Cash deposit");
        return acc;
    }

    /**
     * WITHDRAW money — only if the balance covers it.
     */
    @Transactional
    public Account withdraw(String accountNumber, BigDecimal amount) {
        requirePositive(amount);
        Account acc = accountRepo.findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new ApiException("Account not found", 404));

        if (acc.getBalance().compareTo(amount) < 0) {
            throw new ApiException("Insufficient funds", 400);
        }
        acc.setBalance(acc.getBalance().subtract(amount));
        accountRepo.save(acc);

        record("WITHDRAWAL", amount, acc.getAccountNumber(), null, acc.getBalance(), "Cash withdrawal");
        return acc;
    }

    /**
     * TRANSFER money from one account to another.
     *
     * This is the heart of the system. The whole method is @Transactional, so
     * the debit and the credit happen TOGETHER or NOT AT ALL. If anything throws
     * after we debit the sender but before we credit the receiver, the database
     * rolls the debit back. Money is never created or destroyed.
     */
    @Transactional
    public Account transfer(String fromAccount, String toAccount, BigDecimal amount) {
        requirePositive(amount);

        if (fromAccount.equals(toAccount)) {
            throw new ApiException("Cannot transfer to the same account", 400);
        }

        // Lock both rows for update so a concurrent transfer can't interleave.
        // We lock in a consistent order (by account number) to avoid deadlocks.
        String first = fromAccount.compareTo(toAccount) < 0 ? fromAccount : toAccount;
        String second = first.equals(fromAccount) ? toAccount : fromAccount;
        accountRepo.findByAccountNumberForUpdate(first);
        accountRepo.findByAccountNumberForUpdate(second);

        Account from = accountRepo.findByAccountNumberForUpdate(fromAccount)
                .orElseThrow(() -> new ApiException("Sender account not found", 404));
        Account to = accountRepo.findByAccountNumberForUpdate(toAccount)
                .orElseThrow(() -> new ApiException("Recipient account not found", 404));

        if (from.getBalance().compareTo(amount) < 0) {
            throw new ApiException("Insufficient funds", 400);
        }

        // --- the two-sided move ---
        from.setBalance(from.getBalance().subtract(amount));   // debit sender
        to.setBalance(to.getBalance().add(amount));            // credit receiver
        accountRepo.save(from);
        accountRepo.save(to);

        // --- two audit rows, one for each side ---
        record("TRANSFER_OUT", amount, from.getAccountNumber(), to.getAccountNumber(),
                from.getBalance(), "Transfer to " + to.getAccountNumber());
        record("TRANSFER_IN", amount, to.getAccountNumber(), from.getAccountNumber(),
                to.getBalance(), "Transfer from " + from.getAccountNumber());

        return from;
    }

    // --- helpers ---

    private void requirePositive(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("Amount must be greater than zero", 400);
        }
    }

    private void record(String type, BigDecimal amount, String account,
                        String counterparty, BigDecimal balanceAfter, String desc) {
        txnRepo.save(new Transaction(type, amount, account, counterparty, balanceAfter, desc));
    }
}
