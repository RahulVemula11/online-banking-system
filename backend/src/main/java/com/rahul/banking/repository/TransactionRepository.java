package com.rahul.banking.repository;

import com.rahul.banking.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // "find all transactions for this account, newest first"
    // Spring builds: SELECT * FROM transactions WHERE account_number = ? ORDER BY timestamp DESC
    List<Transaction> findByAccountNumberOrderByTimestampDesc(String accountNumber);
}
