# 🧩 Authentication API (Reactive Spring WebFlux + Gradle)

A reactive authentication and registration microservice built with **Spring Boot (WebFlux)** and **R2DBC (MySQL)**.
The service provides endpoints for user registration and login with **JWT-based authentication**, using non-blocking reactive programming.

---

## ✅ Code Coverage
[![codecov](https://codecov.io/github/baxterrp/io.baxter.authentication/branch/main/graph/badge.svg?token=7W4PYZOXFH)](https://codecov.io/github/baxterrp/io.baxter.authentication)

## 📊 Sonar Quality Gate
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=baxterrp_io-baxter-authentication&metric=alert_status&token=e0b768d87e69c0ac0d508ac9dd567dd92b5c30c3)](https://sonarcloud.io/summary/new_code?id=baxterrp_io-baxter-authentication)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=baxterrp_io-baxter-authentication&metric=code_smells&token=e0b768d87e69c0ac0d508ac9dd567dd92b5c30c3)](https://sonarcloud.io/summary/new_code?id=baxterrp_io-baxter-authentication)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=baxterrp_io-baxter-authentication&metric=duplicated_lines_density&token=e0b768d87e69c0ac0d508ac9dd567dd92b5c30c3)](https://sonarcloud.io/summary/new_code?id=baxterrp_io-baxter-authentication)

[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=baxterrp_io-baxter-authentication&metric=sqale_rating&token=e0b768d87e69c0ac0d508ac9dd567dd92b5c30c3)](https://sonarcloud.io/summary/new_code?id=baxterrp_io-baxter-authentication)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=baxterrp_io-baxter-authentication&metric=security_rating&token=e0b768d87e69c0ac0d508ac9dd567dd92b5c30c3)](https://sonarcloud.io/summary/new_code?id=baxterrp_io-baxter-authentication)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=baxterrp_io-baxter-authentication&metric=reliability_rating&token=e0b768d87e69c0ac0d508ac9dd567dd92b5c30c3)](https://sonarcloud.io/summary/new_code?id=baxterrp_io-baxter-authentication)
## 📘 API Specification
You can view the full OpenAPI specification here:  
👉 [openapi.json](https://github.com/baxterrp/io.baxter.authentication/blob/main/openapi.json)

## 🚀 Features

- Reactive and non-blocking using **Spring WebFlux**
- JWT authentication for stateless security
- Secure password hashing with **BCrypt**
- Role-based access system (Users ↔ Roles)
- Centralized exception handling for clean API responses
- **MySQL** integration via **R2DBC**
- Containerized using **Docker Compose**
- Configurable via environment variables

---

## 🧱 Project Structure

```
io.baxter.authentication
├── .github
|   ├── workflows
|   |   └── gradle-ci.yml  # github actions pipeline for build, test, code coverage report
├── api
│   ├── controllers        # REST endpoints (AccessController)
│   ├── models             # Request/Response DTOs
│   └── services           # AccessService interface + implementation
├── data
│   ├── models             # R2DBC entity models (UserDataModel, RoleDataModel)
│   └── repository         # Reactive repositories (UserRepository, RoleRepository, etc.)
├── infrastructure
│   ├── auth               # JWT token generation + password encryption
│   └── behavior           # Exception handling and domain-specific exceptions
├── docker-compose.yml
└── src/main/resources/application.properties
```

---

## ⚙️ Technology Stack

| Component | Technology |
|-----------|------------|
| Language | Java 17+ |
| Framework | Spring Boot 3.5.x (WebFlux) |
| Database | MySQL 8 (Reactive via R2DBC) |
| Security | JWT + BCrypt |
| Build Tool | Gradle Kotlin DSL |
| Reactive Engine | Project Reactor (Mono / Flux) |
| Containerization | Docker & Docker Compose |

---

## 🧠 Configuration

Environment variables are used for flexibility.  
`application.properties` expects:

```
spring.application.name=io.baxter.api

spring.r2dbc.url=${SPRING_R2DBC_URL}
spring.r2dbc.username=${SPRING_R2DBC_USERNAME}
spring.r2dbc.password=${SPRING_R2DBC_PASSWORD}
jwt.secret=${JWT_SECRET}
jwt.expiration-ms=${JWT_EXPIRATION_MS}

logging.level.root=INFO
logging.level.io.baxter=DEBUG
```

---

## 🐳 Running with Docker Compose

1. **Create a `.env` file** in the project root:

```
MYSQL_ROOT_PASSWORD=rootpassword
MYSQL_DATABASE=authentication_db
MYSQL_USER=authuser
MYSQL_PASSWORD=authpass
MYSQL_PORTS=3306:3306
MYSQL_URL=r2dbc:mysql://db:3306/authentication_db

JWT_SECRET=supersecretkey
JWT_EXPIRATION_MS=3600000

API_PORTS=8080:8080

COMPOSE_PROJECT_NAME=auth
```

2. **Build and start services:**

```bash
docker compose up --build
```

- MySQL container: `authentication-db`
- API container: `authentication-api`
- API available at: `http://localhost:8080/api/auth`

---

## 💻 Running Locally (Gradle)

If you prefer to run without Docker:

1. Start a local MySQL database.
2. Set environment variables or edit `application.properties`:

```
spring.application.name=io.baxter.api

spring.r2dbc.url=r2dbc:mysql://localhost:3306/authentication_db
spring.r2dbc.username=authuser
spring.r2dbc.password=authpass
jwt.secret=yoursecret
jwt.expiration-ms=3600000

logging.level.root=INFO
logging.level.io.baxter=DEBUG
```

3. Run the application with Gradle:

```bash
./gradlew bootRun
```

---

## 🧩 Design Notes

- `AccessServiceImpl` handles registration and login logic.
- Passwords are hashed via `PasswordEncryption` (BCrypt).
- JWTs are generated using `JwtTokenGenerator` with configured secret and expiration.
- Reactive repositories (`Mono`/`Flux`) ensure non-blocking DB operations.
- Global exception handling provides clean HTTP responses for domain errors.

---

### 👤 Author

**Robert Baxter**  
💻 [GitHub](https://github.com/baxterrp)

