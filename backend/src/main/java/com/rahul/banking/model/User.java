package com.rahul.banking.model;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * A User is the login identity. One User owns one Account (kept simple for the demo).
 * The password stored here is ALWAYS a BCrypt hash, never the plain text.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    // BCrypt hash of the password — see PasswordEncoder in SecurityConfig
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    // ROLE_CUSTOMER or ROLE_ADMIN — drives what each user is allowed to do
    @Column(nullable = false)
    private String role = "ROLE_CUSTOMER";

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // One user has one account. Lazy = don't load it until asked.
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Account account;

    public User() { }

    public User(String username, String password, String fullName, String role) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }

    // --- getters and setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
}
