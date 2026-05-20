package com.rahul.banking.repository;

import com.rahul.banking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * A Repository is the door to the database for one entity.
 *
 * By extending JpaRepository<User, Long> we instantly get, for free:
 *   save(), findById(), findAll(), deleteById(), count(), ...
 *
 * We only declare the EXTRA finders we need. Spring reads the method NAME
 * ("findByUsername") and generates the SQL ("SELECT * FROM users WHERE username = ?").
 * No SQL written by hand.
 *
 * Optional<User> = "maybe a user, maybe nobody" — forces us to handle the
 * "username doesn't exist" case instead of crashing on null.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
