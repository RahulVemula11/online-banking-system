# Architecture & Design Decisions

This document explains *how* the system works and *why* it was built this way.
If you're presenting this project, these are the questions you'll be asked — and
the answers.

---

## 1. The layered architecture

Requests flow top to bottom, and each layer has one job:

```
Browser (React)
   │  HTTP + JSON, with "Authorization: Bearer <token>"
   ▼
Controller   ── parses the request, returns the response (thin, no logic)
   ▼
Service      ── the business rules: transfers, deposits, validation (the brain)
   ▼
Repository   ── talks to the database (Spring generates the SQL)
   ▼
Database     ── PostgreSQL in production, H2 in development
```

**Why separate them?** Each layer can be changed or tested independently. The
controller doesn't know SQL; the service doesn't know HTTP. Swapping H2 for
Postgres needed zero code changes — only config.

---

## 2. Authentication — how JWT works here

1. User sends username + password to `/api/auth/login`.
2. We look up the user and compare the password against the stored **BCrypt hash**.
3. If it matches, we generate a **JWT** — a signed token containing the username
   and role, with a 24-hour expiry.
4. The browser stores the token and sends it on **every** later request in the
   `Authorization` header.
5. `JwtAuthFilter` runs on each request, verifies the token's signature, and tells
   Spring Security who the user is.

**Why JWT instead of sessions?** JWTs are *stateless* — the server keeps no
session in memory, so the API scales horizontally (any server can verify any
token). The trade-off: you can't easily invalidate a token before it expires,
which is why expiry is short.

**Q: What stops someone forging a token?** The signature. The token is signed
with a secret only the server knows. Change one character of the payload and the
signature no longer matches, so verification fails.

---

## 3. Password security

- Passwords are **never** stored in plain text — only a **BCrypt hash**.
- BCrypt adds a random **salt** to each hash, so two users with the same password
  get different hashes (defeats rainbow-table attacks).
- BCrypt is deliberately **slow**, which makes brute-forcing expensive.
- We never send the hash back to the frontend — DTOs keep entities and API
  responses separate.

---

## 4. Money safety — the most important part

**Q: Why is balance a `BigDecimal` and not a `double`?**
Floating-point can't represent decimal money exactly (`0.1 + 0.2 != 0.3`).
Over many transactions you'd lose fractions of a cent. `BigDecimal` is exact.

**Q: How do you guarantee a transfer never loses or duplicates money?**
The whole `transfer()` method is `@Transactional`. The debit (sender −) and the
credit (receiver +) happen inside one database transaction. If anything fails
between them, the database **rolls back** both — it's all-or-nothing. Money can
never be debited without being credited.

**Q: What about two transfers happening at the exact same time?**
Two defenses:
- **Pessimistic locking** (`findByAccountNumberForUpdate` uses
  `SELECT ... FOR UPDATE`): the database locks the row so the second transfer
  waits until the first finishes.
- **Optimistic locking** (`@Version` on `Account`): if a row changes underneath a
  transaction, the save fails instead of overwriting with stale data.
- We also lock accounts in a **consistent order** (sorted by account number) so
  two transfers touching the same pair can't deadlock by each holding what the
  other needs.

---

## 5. The transaction log

Every deposit, withdrawal, and transfer writes a `Transaction` row. These are
**append-only** — never edited or deleted. A bank must be able to prove what
happened, so the history is the source of truth. A transfer writes **two** rows:
`TRANSFER_OUT` on the sender's statement, `TRANSFER_IN` on the receiver's.

---

## 6. How a user is prevented from touching someone else's account

The protected endpoints take the username from the **verified token**, not from
the request body:

```java
public AccountResponse transfer(Authentication auth, @RequestBody TransferRequest req) {
    Account acc = accountService.getByUsername(auth.getName()); // from the token
    ...
}
```

So even if an attacker edits the request, they can only ever act on the account
tied to their own token. They choose the *recipient*, never the *sender*.

---

## 7. What I'd add next (good to mention)

- Daily transfer limits and fraud checks
- Email/OTP verification on login (2FA)
- Refresh tokens + token revocation list
- Pagination on transaction history
- Integration tests for concurrent transfers

These show you know the project isn't "finished" — it's a solid core with a
clear path forward.
