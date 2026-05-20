# Aegis Bank — Online Banking System

A full-stack online banking application: secure account management, deposits,
withdrawals, money transfers between accounts, and a complete transaction history.

**Stack:** Java 17 · Spring Boot 3 · Spring Security · JWT · JPA/Hibernate ·
PostgreSQL (prod) / H2 (dev) · React 18 (Vite)

---

## What it does

- **Register / log in** with JWT-based authentication
- **View balance** and a generated account number
- **Deposit** and **withdraw** money with validation
- **Transfer** money to another account — atomic, all-or-nothing
- **Transaction history** — an append-only audit log of every movement
- **Roles** — `ROLE_CUSTOMER` and `ROLE_ADMIN`

## Demo logins (seeded automatically on first run)

| Username | Password      | Starting balance |
|----------|---------------|------------------|
| `alice`  | `password123` | ₹5,000.00        |
| `bob`    | `password123` | ₹1,500.00        |
| `admin`  | `admin123`    | ₹0.00 (admin)    |

Try transferring from `alice` to `bob`'s account number (`BANK10000002`).

---

## Run it locally

You need **Java 17+** and **Node 18+**. No database install required for local dev
(it uses an in-memory H2 database).

### 1. Backend (port 8080)

```bash
cd backend
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

The API is now at `http://localhost:8080`. Inspect the database at
`http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:bankdb`, user `sa`, no password).

### 2. Frontend (port 5173)

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173` and log in with a demo account.

---

## Project structure

```
banking-system/
├── backend/                     Spring Boot REST API
│   └── src/main/java/com/rahul/banking/
│       ├── model/               JPA entities (User, Account, Transaction)
│       ├── repository/          Spring Data repositories
│       ├── service/             Business logic (transfers, auth)
│       ├── controller/          REST endpoints
│       ├── security/            JWT + Spring Security
│       ├── dto/                 Request/response shapes
│       ├── exception/           Centralized error handling
│       └── config/              Security config + demo data seeder
└── frontend/                    React + Vite single-page app
    └── src/
        ├── api/                 API client (attaches JWT)
        ├── pages/               Login, Dashboard
        └── styles.css
```

## API reference

| Method | Endpoint                 | Auth | Purpose                    |
|--------|--------------------------|------|----------------------------|
| POST   | `/api/auth/register`     | —    | Create user + account      |
| POST   | `/api/auth/login`        | —    | Get a JWT                  |
| GET    | `/api/account`           | JWT  | Current balance            |
| POST   | `/api/account/deposit`   | JWT  | Deposit                    |
| POST   | `/api/account/withdraw`  | JWT  | Withdraw                   |
| POST   | `/api/account/transfer`  | JWT  | Transfer to another acct   |
| GET    | `/api/account/statement` | JWT  | Account + full history     |

## Deployment

See [`DEPLOYMENT.md`](./DEPLOYMENT.md) for free hosting on Render (backend + Postgres)
and Vercel (frontend).

## Design notes

See [`ARCHITECTURE.md`](./ARCHITECTURE.md) for how authentication, money-safety,
and the transfer logic actually work.

---

Built by Rahul Bhushan Vemula.
