# Banking-backed E-commerce Platform

A distributed system combining a **banking transaction backend** (Kafka exactly-once semantics, idempotent transfers, double-entry ledger) with a full **e-commerce platform** (catalog, cart, auth, orders, payments) that settles through it — built with **11 microservices**, **Apache Kafka** as the event backbone, and **Redis** for idempotency and cart state. Deployed on **AWS EC2 + RDS**.

---

## 🏗️ Architecture

```
┌──────────────┐     ┌───────────────┐     ┌──────────────────┐
│ Client       │────▶│  API Gateway  │────▶│ Catalog / Cart /  │
│              │     │               │     │ Auth / Order      │
└──────────────┘     └───────────────┘     └─────────┬─────────┘
                                                       │ checkout
                                                       ▼
                                            ┌──────────────────────┐
                                            │ Payment              │
                                            │ Orchestration Service│
                                            └──────────┬───────────┘
                                    webhook (card/UPI)  │  COD
                                                       ▼
                                            ┌──────────────────────┐
                                            │  Kafka (Events)      │
                                            └──────────┬───────────┘
                                                       ▼
                          ┌────────────────────────────┼────────────────────────────┐
                          ▼                            ▼                            ▼
                ┌──────────────────┐        ┌──────────────────┐         ┌──────────────────┐
                │ Transaction      │        │ Ledger           │         │ Audit            │
                │ Service (EOS)    │───────▶│ Service          │         │ Service          │
                └──────────────────┘        └──────────────────┘         └──────────────────┘
                          │
                          ▼
                ┌──────────────────┐
                │ Notification     │
                │ Service          │
                └──────────────────┘
```

### Event Flow

**Checkout → settlement:**
1. `OrderCreated` → Order Service (status: `PENDING_PAYMENT`)
2. Payment call → Payment Orchestration Service (card/UPI/COD)
3. Gateway webhook → `payment.confirmed` / `payment.failed` (Kafka)
4. `TransactionExecuted` → Transaction Service (idempotent, exactly-once)
5. `LedgerEntryWritten` + `AuditLogWritten` → Ledger & Audit Services (independent consumers)
6. `OrderConfirmed` → Order Service updates status
7. Notification sent → Notification Service

**Failure flow:**
- `PaymentFailed` → Order marked `FAILED` → Customer notified → no ledger entry written

---

## 🧰 Tech Stack

| Category           | Technology                                                       |
|--------------------|--------------------------------------------------------------------|
| Language           | Java 17+                                                          |
| Framework          | Spring Boot 3.x, Spring Cloud Gateway, Spring Security (OAuth2)   |
| Messaging          | Apache Kafka (exactly-once semantics)                             |
| Caching            | Redis (idempotency keys, cart storage)                            |
| Databases          | PostgreSQL (per-service), MongoDB (catalog)                       |
| Search             | Elasticsearch (category filtering, planned)                       |
| Containerization   | Docker, Docker Compose                                            |
| Cloud              | AWS EC2, AWS RDS, AWS ECR                                         |
| CI/CD              | GitHub Actions                                                    |
| Testing            | JUnit 5, fault-injection tests                                    |

---

## 📦 Services

| Service                        | Port | Database          | Responsibility                              |
|----------------------------------|------|--------------------|-----------------------------------------------|
| `api-gateway`                    | 8080 | —                  | Single entry point, routing                  |
| `account-service`                | 8081 | Postgres           | Customer accounts, balances                  |
| `transaction-service`            | 8082 | Postgres + Redis   | Transfers, idempotency, Kafka EOS producer   |
| `ledger-service`                 | 8083 | Postgres           | Double-entry ledger, Kafka consumer          |
| `audit-service`                  | 8084 | Postgres           | Append-only audit trail, Kafka consumer      |
| `notification-service`           | 8085 | —                  | Email/SMS on order & payment events          |
| `product-catalog-service`        | 8086 | MongoDB            | Products, categories, sales flags            |
| `cart-service`                   | 8087 | Redis              | Per-user shopping cart                       |
| `user-auth-service`              | 8088 | Postgres           | Login (email + Google OAuth), profiles       |
| `order-service`                  | 8089 | Postgres           | Order lifecycle and status                   |
| `payment-orchestration-service`  | 8090 | Postgres           | Card/UPI/COD, payment gateway webhooks       |

---

## 📁 Repository Structure

```
banking-ecommerce-platform/
├── services/
│   ├── api-gateway/
│   ├── account-service/
│   ├── transaction-service/
│   ├── ledger-service/
│   ├── audit-service/
│   ├── notification-service/
│   ├── product-catalog-service/
│   ├── cart-service/
│   ├── user-auth-service/
│   ├── order-service/
│   └── payment-orchestration-service/
├── infra/
│   └── postgres-init/
├── docs/
│   └── architecture.md
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Docker & Docker Compose
- Maven

### Run locally
```bash
git clone https://github.com/<Saptarshi-iitbhu>/banking-ecommerce-platform.git
cd banking-ecommerce-platform
cp .env.example .env
docker-compose up --build
```

This spins up all 11 services along with Kafka, Redis, PostgreSQL, and MongoDB.

Check a service is alive:
```bash
curl http://localhost:8081/ping
```

Kafka UI (inspect topics/messages): `http://localhost:8888`

---

## 🧪 Testing

```bash
cd services/<service-name>
mvn test
```

- JUnit 5 unit + integration tests per service
- Fault-injection tests validating idempotency and Kafka retry behavior
- Target: 99.9% transaction consistency under simulated broker/network failure

---

## 📊 Observability (planned)

- Centralized logging via CloudWatch / ELK
- Distributed tracing via AWS X-Ray
- Metrics dashboards via Prometheus + Grafana

---

## 📌 Roadmap

- [ ] Core service scaffolding (Spring Boot + per-service databases)
- [ ] Docker Compose local environment (Postgres, MongoDB, Redis, Kafka)
- [ ] Payment webhook handler (signature verification + idempotency)
- [ ] Redis-backed idempotency service (transaction-service)
- [ ] Kafka exactly-once semantics wiring (transaction → ledger → audit)
- [ ] Product catalog + cart implementation
- [ ] User auth (email + Google OAuth)
- [ ] Order → payment → settlement end-to-end flow
- [ ] CI/CD via GitHub Actions
- [ ] AWS EC2 + RDS deployment
- [ ] Observability stack (Prometheus/Grafana)

---

## 📄 License

This project is licensed under the MIT License.