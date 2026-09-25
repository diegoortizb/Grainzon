# Grainzon

Minimal products app: Spring Boot 4 (Java 25) REST API, React + Vite frontend, PostgreSQL with Flyway migrations.

## Prerequisites

- JDK 25
- Node 24+
- Docker Desktop

## Run locally

```sh
npm install      # first time only: installs root + frontend dependencies
npm run dev      # starts Postgres, backend (:8080) and frontend (:3000)
```

Open http://localhost:3000. Ctrl+C stops the backend and frontend; Postgres keeps running.

To run the pieces separately, use the two sections below, each in its own terminal.

## Start backend

```sh
npm run db:up        # Postgres, if it isn't running yet
npm run dev:api      # Spring Boot on http://localhost:8080
```

Without npm: `cd backend`, then `./gradlew bootRun` (macOS/Linux) or `gradlew.bat bootRun` (Windows). Flyway creates the `products` table on startup.

## Start frontend

```sh
npm run dev:web      # Vite on http://localhost:3000
```

Without npm scripts at the root: `cd frontend && npm run dev`. Requests to `/api` are forwarded to the backend on :8080.

| Command           | What it does                                  |
|-------------------|-----------------------------------------------|
| `npm run dev`     | Postgres + backend + frontend in one terminal |
| `npm run build`   | Backend jar and frontend bundle               |
| `npm test`        | Backend tests, frontend lint and frontend tests |
| `npm run db:seed` | Replace all products with 250 samples from `scripts/seed.sql` (Postgres must be running) |
| `npm run db:clear` | Delete all products and reset ids to 1 (Postgres must be running) |
| `npm run db:down` | Stop Postgres                                 |

The Vite dev server proxies `/api` to the backend.

## Run tests

No database needed. From the repo root:

```sh
npm test                                                  # everything: backend tests, frontend lint, frontend tests
node scripts/gradlew.mjs test                             # backend only (add --rerun to force a run when nothing changed)
node scripts/gradlew.mjs test --tests ProductServiceTests # one backend test class
npm --prefix frontend test                                # frontend only
npm --prefix frontend run test:watch                      # frontend, re-running on save
```

The backend test report is at `backend/build/reports/tests/test/index.html`.

On Windows, run these from PowerShell or cmd. A WSL terminal can't see the Windows JDK. If you're in WSL, run `cmd.exe /c gradlew.bat test` from `backend/`.

## API

| Method | Path            | Body               | Response                  |
|--------|-----------------|--------------------|---------------------------|
| GET    | `/api/products?page=0&size=10` |     | `200` one page: `{content, page, size, totalElements, totalPages}`. `page` is zero-based (default 0); `size` 1–100 (default 10) |
| POST   | `/api/products` | `{"name": "..."}`  | `201` created product, `400` if name is blank or over 256 characters (surrounding whitespace is trimmed first) |

## Database

Schema is managed by Flyway (`backend/src/main/resources/db/migration`). Add changes as new `V<n>__description.sql` files; never edit an applied migration.

Connection defaults to `jdbc:postgresql://localhost:5432/grainzon` (user/password `grainzon`), overridable with `DB_URL`, `DB_USER`, `DB_PASSWORD`.
