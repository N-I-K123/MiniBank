# 🏦 MiniBank API

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4-green)
![Security](https://img.shields.io/badge/Spring_Security-JWT-red)
![Database](https://img.shields.io/badge/Database-H2-blue)

A robust REST API for a banking system, featuring account management, fund transfers, currency exchange, and transaction history. The project was built following Clean Architecture principles and best practices (SOLID, DTO pattern, Comprehensive Testing).

## 🚀 Features

* **Authentication & Security:** User registration, login, and endpoint protection using JWT (JSON Web Tokens).
* **Bank Accounts:** Creation of accounts in multiple currencies (PLN, USD, EUR).
* **Fund Transfers:** Internal transfers with balance validation and concurrency control.
* **Currency Exchange:** Automatic currency conversion for transfers between different currency accounts (integrated with external NBP API).
* **Transaction History:** Full log of incoming and outgoing transactions.
* **Validation:** Robust input validation (`@Valid`, Regex for emails, custom constraints).
* **Documentation:** Auto-generated, interactive API documentation via OpenAPI (Swagger).

## 🛠️ Tech Stack

* **Core:** Java 21, Spring Boot 3
* **Database:** H2 (In-memory database for dev/test)
* **ORM:** Hibernate / Spring Data JPA
* **Security:** Spring Security 6, JWT
* **Testing:** JUnit 5, Mockito, Spring Boot Test (Integration Tests)
* **Documentation:** SpringDoc OpenAPI (Swagger UI)
* **Build Tool:** Maven

## ⚙️ Getting Started

### Prerequisites
* Java 21 (JDK)
* Maven (Wrapper included in the project)

### Installation

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/N-I-K123/minibank.git
    cd minibank
    ```

2.  **Run the application:**
    ```bash
    ./mvnw spring-boot:run
    ```
    *(For Windows users: `mvnw.cmd spring-boot:run`)*

3.  **Done!** The application is running at: `http://localhost:8080`

## 📖 API Documentation (Swagger)

Once the application is running, you can access the full, interactive API documentation here:
👉 **[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**

![Swagger Screenshot](assets/swagger.png)

## 📚 Database Schema (Class Diagram)

Below is the Entity Relationship Diagram showing the database structure:

![ClassDiagram Screenshot](assets/ClassDiagram.png)

## ✅ Testing

The project maintains high code coverage with both Unit and Integration tests. To run them:

```bash
./mvnw test