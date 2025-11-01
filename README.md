# 🧩 Authentication API (Reactive Spring WebFlux + Gradle)

A reactive authentication and registration microservice built with **Spring Boot (WebFlux)** and **R2DBC (MySQL)**.
The service provides endpoints for user registration and login with **JWT-based authentication**, using non-blocking reactive programming.

---

## Code Coverage
[![codecov](https://codecov.io/github/baxterrp/io.baxter.authentication/branch/main/graph/badge.svg?token=7W4PYZOXFH)](https://codecov.io/github/baxterrp/io.baxter.authentication)

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
|    
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

## 🔐 API Endpoints

### POST /api/auth/register

Registers a new user and assigns roles.

**Request Body:**

```
{
  "userName": "alice",
  "password": "password123",
  "roles": ["USER"]
}
```

**Response (201 Created):**

```
{
  "name": "alice",
  "userId": 1
}
```

---

### POST /api/auth/login

Authenticates a user and returns a JWT token.

**Request Body:**

```
{
  "userName": "alice",
  "password": "password123"
}
```

**Response (200 OK):**

```
{
  "token": "eyJhbGciOiJIUzI1NiIsInR..."
}
```

**Error (401 Unauthorized):**

```
{
  "error": "Invalid credentials"
}
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

