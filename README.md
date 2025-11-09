# 🧩 Authentication API (Reactive Spring WebFlux + Gradle)

A reactive authentication and registration microservice built with **Spring Boot (WebFlux)** and **R2DBC (MySQL)**.
The service provides endpoints for user registration and login with **JWT-based authentication**, using non-blocking reactive programming.
Now includes secure rotating refresh token sessions backed by Redis.

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
- JWT authentication for stateless access token validation
- **Rotating refresh token support stored in Redis**
- Secure password hashing with **BCrypt**
- Role-based access control
- Centralized exception handling for consistent API error responses
- **MySQL** via **R2DBC** (fully non-blocking)
- Containerized with **Docker Compose**
- Environment-based configuration for portability

---

## 🧱 Project Structure

```
io.baxter.authentication
├── api
│ ├── controllers # REST endpoints (login, register, refresh)
│ ├── models # Request/Response DTOs
│ └── services # AccessService interface + AccessServiceImpl
├── data
│ ├── models # UserDataModel, RoleDataModel, UserRoleDataModel
│ └── repository # Reactive R2DBC repositories
├── infrastructure
│ ├── auth # JwtTokenGenerator, PasswordEncryption, Security config
│ ├── behavior
│ │ ├── exceptions # Domain + validation exceptions
│ │ ├── handlers # Global exception handling
│ │ └── helper # Shared utilities (e.g., factory helpers)
│ ├── redis # Reactive Redis config + token serialization
│ └── config # OpenAPI config, DateTime Clock config
├── src/main/resources
│ └── application.properties
├── docker-compose.yml
└── Application.java
```
---
## 🔁 Refresh Token Flow

**On login**
- Generate short-lived **Access Token (JWT)**
- Generate long-lived **Refresh Token ID** (UUID)
- **Store in Redis**:
    - **key:** `refresh_token:{id}`
    - **value:** `{ username, roles[], issuedAt, expiresAt }`
    - **TTL:** matches refresh lifetime (e.g., 30 days)
- Return `{ accessToken, refreshTokenId }` to the client

**On refresh**
1. Client sends `refreshTokenId`
2. Load `refresh_token:{id}` from Redis
3. If missing → **401**
4. If `expiresAt` < **now** → delete Redis key → **401**
5. Generate **new access token**
6. **Rotate** refresh token:
    - Create **new** `refreshTokenId`
    - Save new Redis value with TTL
    - Delete **old** Redis key
7. Return `{ accessToken, refreshTokenId: newId }`
---

## ⚙️ Technology Stack
| Layer | Technology |
|------|------------|
| Language | **Java 21** |
| Framework | **Spring Boot 3.5+** (WebFlux) |
| Reactive Engine | **Project Reactor (Mono / Flux)** |
| Security | **JWT (HS256)** for access tokens, **BCrypt** for password hashing |
| Refresh Token Store | **Redis 7** (Reactive RedisTemplate) |
| Database | **MySQL 8** (R2DBC Reactive Driver) |
| Build Tool | **Gradle 8.7+** (Kotlin DSL) |
| API Documentation | **OpenAPI / Swagger** (springdoc-openapi) |
| Configuration | Environment-based (`application.properties` + Docker `.env`) |
| Containerization | **Docker & Docker Compose** |
| CI/CD | **GitHub Actions** + Codecov + SonarCloud |

---

## 🧠 Configuration

Environment variables are used for flexibility.  
`application.properties` expects:

```
spring.application.name=io.baxter.authentication

spring.r2dbc.url=${SPRING_R2DBC_URL}
spring.r2dbc.username=${SPRING_R2DBC_USERNAME}
spring.r2dbc.password=${SPRING_R2DBC_PASSWORD}
spring.security.oauth2.resourceserver.jwt.secret-key=${JWT_SECRET}

jwt.expiration-ms=${JWT_EXPIRATION_MS}

logging.level.root=INFO
logging.level.io.baxter=DEBUG

spring.data.redis.host=${SPRING_REDIS_HOST}
spring.data.redis.port=${SPRING_REDIS_PORT}
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

API_URL=http://localhost:9000
API_PORTS=9000:8080

COMPOSE_PROJECT_NAME=auth
```

2. **Build and start services:**

```bash
docker compose up --build
```

- Redis container: `authenticaiton-redis`
- MySQL container: `authentication-db`
- API container: `authentication-api`
- API available at: `http://localhost:8080/api/auth`

---

## 💻 Running Locally (Gradle)

If you prefer to run without Docker:

1. Start a local MySQL database.
2. Set environment variables or edit `application.properties`
3. Run the application with Gradle:

```bash
./gradlew bootRun
```
---

### 👤 Author

**Robert Baxter**  
💻 [GitHub](https://github.com/baxterrp)

