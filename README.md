# Grainzon

Minimal products app: Spring Boot 4 (Java 25) REST API, React + Vite frontend, PostgreSQL with Flyway migrations.

## Prerequisites

- JDK 25
- Node 24+
- Docker Desktop

## Run locally

```sh
npm install      # first time only: installs root + frontend dependencies
npm run dev      # starts Postgres, backend (:8080) and frontend (:5173)
```

Open http://localhost:5173. Ctrl+C stops the backend and frontend; Postgres keeps running.

| Command           | What it does                                  |
|-------------------|-----------------------------------------------|
| `npm run dev`     | Postgres + backend + frontend in one terminal |
| `npm run build`   | Backend jar and frontend bundle               |
| `npm test`        | Backend tests and frontend lint               |
| `npm run db:down` | Stop Postgres                                 |

The Vite dev server proxies `/api` to the backend.

## API

| Method | Path            | Body               | Response                  |
|--------|-----------------|--------------------|---------------------------|
| GET    | `/api/products` |                    | `200` list of products    |
| POST   | `/api/products` | `{"name": "..."}`  | `201` created product, `400` if name is blank |

## Database

Schema is managed by Flyway (`backend/src/main/resources/db/migration`). Add changes as new `V<n>__description.sql` files; never edit an applied migration.

Connection defaults to `jdbc:postgresql://localhost:5432/grainzon` (user/password `grainzon`), overridable with `DB_URL`, `DB_USER`, `DB_PASSWORD`.
