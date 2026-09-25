# Grainzon

Minimal products app: Spring Boot 4 (Java 25) REST API, React + Vite frontend, PostgreSQL with Flyway migrations.

## Prerequisites

- JDK 25
- Node 24+
- Docker Desktop

## Run locally

```sh
# 1. Start Postgres
docker compose up -d

# 2. Start the backend (http://localhost:8080)
cd backend
./gradlew bootRun

# 3. Start the frontend (http://localhost:5173)
cd frontend
npm install
npm run dev
```

The Vite dev server proxies `/api` to the backend.

## API

| Method | Path            | Body               | Response                  |
|--------|-----------------|--------------------|---------------------------|
| GET    | `/api/products` |                    | `200` list of products    |
| POST   | `/api/products` | `{"name": "..."}`  | `201` created product, `400` if name is blank |

## Database

Schema is managed by Flyway (`backend/src/main/resources/db/migration`). Add changes as new `V<n>__description.sql` files; never edit an applied migration.

Connection defaults to `jdbc:postgresql://localhost:5432/grainzon` (user/password `grainzon`), overridable with `DB_URL`, `DB_USER`, `DB_PASSWORD`.

## Tests

```sh
cd backend
./gradlew test
```
