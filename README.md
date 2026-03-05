# EasyPay API - Transactional MVP

This project is a backend MVP simulating a secure digital wallet ecosystem. It was designed with a strong focus on **data consistency, transactional safety, and clean architecture**, demonstrating how to handle core financial operations reliably.

Built to showcase production-ready practices, the API is fully containerized using **Docker**, relies on a robust **PostgreSQL** database, and implements foundational security measures to protect user data.

## 🛠️ Tech Stack
* **Java 17** & **Spring Boot 3**
* **PostgreSQL** (via Docker)
* **Spring Data JPA** & **Hibernate**
* **Spring Security** (BCrypt for password hashing)
* **JUnit 5** & **Mockito** (Unit Testing)

## 💡 Core Business Rules Implemented
* **User Ecosystem:** Two types of users (`COMMON` and `MERCHANT`). Both are initialized with a digital wallet.
* **Transaction Constraints:** Merchants can only receive money; they cannot initiate transfers.
* **Self-Transfer Block:** Users cannot transfer money to their own wallets.
* **Overdraft Prevention:** Transfers are blocked if the sender has insufficient funds.

## 🏗️ Technical Highlights & Decisions

To deliver a production-ready approach within a short deadline, the following technical decisions were made:

* **Atomic Transactions:** The transfer logic is wrapped in `@Transactional` to ensure ACID properties. If any step fails (e.g., external authorization), the entire database operation is rolled back to prevent money loss.
* **Concurrency & Idempotency:** Implemented an `idempotency_key` constraint at the database level to prevent duplicate transactions (double-spending) in case of frontend retries or race conditions.
* **Precision Matters:** Used `BigDecimal` for all monetary values instead of floating-point numbers to prevent rounding errors.
* **Secure Storage:** Passwords are never stored or logged in plain text. Used `BCryptPasswordEncoder` for secure hashing.
* **Centralized Error Handling:** Implemented a `@RestControllerAdvice` to intercept exceptions and return standardized, elegant JSON error responses (e.g., `422 Unprocessable Entity` for business rule violations).

## 🚀 How to Run

**1. Start the infrastructure (Database)**
Make sure you have Docker installed and run:
```bash
docker-compose up -d
```

**2. Run the application**
```bash
./mvnw spring-boot:run
```

The API will be available at http://localhost:8080.

## 🧪 Running Tests
Unit tests were written following the Arrange, Act, Assert (AAA) pattern focusing on the core business logic (Transaction Service).

```bash
./mvnw test
```

## 📌 Future Improvements (Out of MVP Scope)
While the core MVP transactional flow is solid, the following features are mapped for future iterations:

- Data Retrieval: Implement GET endpoints for user profiles, wallet balances, and paginated transaction history (statements).

- Advanced Validation: Add real-world algorithmic validation for documents (e.g., CPF/CNPJ) and deep email verification.

- Authentication: Implement stateless JWT Authentication for secure endpoint access.

- Asynchronous Processing: Introduce a message broker (RabbitMQ/Kafka) for sending transaction notifications.

- Pessimistic Locking: Apply database-level row locking on wallets to prevent race conditions in extreme high-concurrency scenarios.
