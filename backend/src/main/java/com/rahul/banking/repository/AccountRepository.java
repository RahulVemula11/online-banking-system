package com.rahul.banking.repository;

import com.rahul.banking.model.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    Optional<Account> findByUser_Username(String username);

    /**
     * Pessimistic lock version used during transfers. PESSIMISTIC_WRITE tells
     * the database "lock this row — nobody else can change it until I'm done."
     * Combined with the @Version field on Account, this is how two simultaneous
     * transfers can't corrupt a balance. We'll use this in TransferService.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :acc")
    Optional<Account> findByAccountNumberForUpdate(@Param("acc") String accountNumber);
}
