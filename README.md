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

| Command            | What it does                                                                             |
|--------------------|------------------------------------------------------------------------------------------|
| `npm run dev`      | Postgres + backend + frontend in one terminal                                            |
| `npm run build`    | Backend jar and frontend bundle                                                          |
| `npm test`         | Backend tests, frontend lint and frontend tests                                          |
| `npm run db:seed`  | Replace all products with 250 samples from `scripts/seed.sql` (Postgres must be running) |
| `npm run db:clear` | Delete all products and reset ids to 1 (Postgres must be running)                        |
| `npm run db:down`  | Stop Postgres                                                                            |

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

## Verify

1. Open http://localhost:3000. You land on **All products**.
2. Click **Add product**, type `P1` and press **Add**. You should see `Added “P1”.`
3. Click **All products** and confirm `P1` is in the list. The list is sorted by id, so if the database already has products (for example after `npm run db:seed`), `P1` is on the last page.

The same check against the API alone:

```sh
curl -X POST http://localhost:8080/api/products -H "Content-Type: application/json" -d '{"name":"P1"}'
curl "http://localhost:8080/api/products?page=0&size=10"
```

## Run tests

The backend integration tests start their own throwaway Postgres in Docker (Testcontainers), so **Docker must be running**. You don't need `npm run db:up`, and your dev data isn't touched. From the repo root:

```sh
npm test                                                  # everything: backend tests, frontend lint, frontend tests
node scripts/gradlew.mjs test                             # backend only (add --rerun to force a run when nothing changed)
node scripts/gradlew.mjs test --tests ProductServiceTests # one backend test class
npm --prefix frontend test                                # frontend only
npm --prefix frontend run test:watch                      # frontend, re-running on save
```

Backend reports, written on every test run:

- Test results: `backend/build/reports/tests/test/index.html`
- Coverage (JaCoCo): `backend/build/reports/jacoco/test/html/index.html`, plus `jacocoTestReport.xml` next to it for CI tools

On Windows, run these from PowerShell or cmd. A WSL terminal can't see the Windows JDK. If you're in WSL, run `cmd.exe /c gradlew.bat test` from `backend/`.

## API

| Method | Path            | Body               | Response                  |
|--------|-----------------|--------------------|---------------------------|
| GET    | `/api/products?page=0&size=10` |     | `200` one page: `{content, page, size, totalElements, totalPages}`. `page` is zero-based (default 0); `size` 1–100 (default 10) |
| POST   | `/api/products` | `{"name": "..."}`  | `201` created product, `400` if name is blank, over 256 characters or contains control characters such as NUL, tab or newline (surrounding whitespace is trimmed first) |

## Database

Schema is managed by Flyway (`backend/src/main/resources/db/migration`). Add changes as new `V<n>__description.sql` files; never edit an applied migration.

Connection defaults to `jdbc:postgresql://localhost:5432/grainzon` (user/password `grainzon`), overridable with `DB_URL`, `DB_USER`, `DB_PASSWORD`.

## Errors and logging

Errors are returned as `application/problem+json` (RFC 9457). Validation errors add an `errors` map from field to reason, and the UI shows that reason:

```json
{"status":400,"title":"Bad Request","detail":"Invalid request.","instance":"/api/products","errors":{"name":"must be at most 256 characters"}}
```

Unexpected errors return `500` with the detail `Something went wrong.`, and are logged at `ERROR` with the stack trace. The response never includes internal details.

The app logs at `INFO` by default: one line per created product. For debug logs (each list request, each create, each rejected request and the reason), change this line in `backend/src/main/resources/application.properties` and restart the backend:

```properties
logging.level.com.grainzon=DEBUG
```

Set it back to `INFO` before committing, so debug logging doesn't reach production.

## Notes

- **Stack:** the preferred one (React, Spring Boot, Postgres). Java 25, Spring Boot 4.1, React 19, Vite, PostgreSQL 18. The backend builds with Gradle (Groovy DSL). Flyway manages the schema.
- **Frontend port:** Vite is set to port 3000.
- **Beyond the minimum:**
  - Pagination: 10, 50 or 100 per page, kept in the URL. Pages past the end move to the last page.
  - A 256-character name limit, enforced by API validation and the input field. The column stays `TEXT`, so the limit can change without a migration. Names are trimmed before validation.
  - Long names are truncated in the list and expand on click.
  - Unit tests on both ends, plus backend integration tests against a real Postgres (Testcontainers). Backend line coverage is 100%.
  - Seed and clear scripts for the database.
- **Known gaps:**
  - No metrics, health checks or CI setup.
  - No end-to-end browser test (for example Playwright) covering the UI and API together.

## How you built it

- **Tools:** Claude Code in VS Code throughout. I used it to review the app against the brief and implement changes, and to verify each one with tests and against the running app (API calls). I reviewed every change and made the commits myself.
- **Repeatable process:** `CLAUDE.md` records the project layout and conventions so an AI assistant or a teammate works the same way: a layered backend, schema changes only through new Flyway migrations, the README updated in the same change, and `npm test` before finishing.
