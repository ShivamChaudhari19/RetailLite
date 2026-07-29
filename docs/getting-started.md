# Getting Started

## Prerequisites

- Java 21
- Maven (or use the bundled `./mvnw`)
- MySQL 8 (or Docker)
- A Razorpay test account (for online payments)

## Environment variables

The app reads all secrets from environment variables (see `application.yaml`). Create a `.env` file
(used by `compose.yaml`) or export these directly:

| Variable | Purpose |
|---|---|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | MySQL connection |
| `JWT_SECRET_KEY` | HMAC signing key for JWTs |
| `TOKEN_EXPIRATION_TIME_IN_MINUTE` | JWT expiry (minutes) |
| `AWS_REGION`, `AWS_ACCESS_KEY`, `AWS_SECRET_KEY`, `BUCKET_NAME` | S3 storage backend |
| `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET`, `RAZORPAY_WEBHOOK_SECRET` | Razorpay gateway |

## Run with Docker Compose (recommended)

```bash
docker compose up --build
```

This starts:
- `mysql` — MySQL 8.0.32 on host port `3307`, database `retail_lite`
- `app` — the Spring Boot app (built via the multi-stage `Dockerfile`) on host port `8080`

## Run locally

```bash
./mvnw spring-boot:run
```

Ensure MySQL is reachable and all environment variables above are exported first.

## Explore the API

Swagger UI (via `springdoc-openapi`) is publicly accessible:

```
http://localhost:8080/api/v1.0/swagger-ui/index.html
```

## Seed / first login

The schema (all tables) and the first `ROLE_ADMIN` account are created automatically on startup by
Flyway (`src/main/resources/db/migration/V1__init.sql`) — nothing
manual to run. Log in with:

- **username:** `admin@retaillite.com`
- **password:** `admin@retaillite`

Rotate this password (or create a new admin and disable this one) before any real deployment — see
[Design Decisions](design-decisions.md) and [Deployment Guide](../gitIgnoreNotes/deployment.md).

## Run tests

```bash
./mvnw test
```

Tests use H2 (in-memory) and `spring-security-test`; see `src/test/resources/application-test.yml`.
