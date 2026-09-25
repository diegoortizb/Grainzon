 # Grainzon

A minimal products app: list products (paged, 10, 50 or 100 per page; default 10) and create new ones. There is no update or delete.

- **backend/**: Spring Boot 4.1 on Java 25, built with Gradle (Groovy `build.gradle`). REST API under `/api/products`.
- **frontend/**: React 19 + TypeScript + Vite, with React Router. Two tabs: `/products` (paged list) and `/products/new` (add form).
- **Database**: PostgreSQL 18 via `docker-compose.yml`. Flyway owns the schema. There is one table, `products` (`id` integer identity, `name` text not null).

The README is the human-facing guide. Read it for run steps, the API contract and database settings.

## Commands (run from the repo root)

- `npm install`: installs root and frontend dependencies (first time only)
- `npm run dev`: starts Postgres, the backend on :8080 and the frontend on :3000 together
- `npm run dev:api` / `npm run dev:web`: start only the backend or only the frontend (`dev:api` needs Postgres running: `npm run db:up`)
- `npm test`: runs the backend tests (`gradlew test`) and the frontend lint (oxlint)
- `npm run build`: builds the backend jar and the frontend bundle
- `npm run db:up` / `npm run db:down`: start or stop Postgres
- `npm run db:seed`: replaces all products with 250 sample rows from `scripts/seed.sql` (realistic names, including quotes, `&` and one 256-character name, then numbered fillers). It is deliberately not a Flyway migration, so sample data never reaches other environments.
- `npm run db:clear`: deletes all products and resets ids to 1. It keeps the schema and Flyway history, and Postgres must be running. For a full wipe, use `docker compose down -v` (Flyway recreates the schema on the next backend start).

`scripts/gradlew.mjs` runs `gradlew.bat` or `./gradlew` depending on the OS. Use it (or the npm scripts) instead of hard-coding either one.

## Backend layout (`backend/src/main/java/com/grainzon/`)

Organized by layer:

- `controller/`: HTTP only (routing, `@Valid`, status codes). Calls the service and never the repository.
- `service/`: business logic and `@Transactional` boundaries. Maps entities to DTOs.
- `repository/`: Spring Data JPA interfaces
- `entity/`: JPA entities
- `dto/`: request and response records. `PageResponse<T>` is the paged JSON shape (`content, page, size, totalElements, totalPages`).

Tests mirror this layout: `@WebMvcTest` for controllers (service mocked) and plain Mockito for services.

## Frontend layout (`frontend/src/`)

- `api/`: the only place that calls `fetch`
- `types/`: shapes of the API's JSON
- `hooks/`: data loading (for example `useProducts(page, size)`)
- `components/`: reusable UI (`Layout` with the tabs, `Pagination`)
- `pages/`: one component per route
- `App.tsx`: route table. `index.css` holds all styles.

The list page keeps its state in the URL: `?page=` is one-based (zero-based in the API) and `?size=` is 10, 50 or 100. Defaults are left out of the URL, invalid values fall back to them, and changing the size returns to page 1. Vite forwards `/api` requests to `localhost:8080`.

## Conventions

- **Schema changes** go in a new `backend/src/main/resources/db/migration/V<n>__description.sql`. Never edit a migration that has already been applied. Hibernate runs with `ddl-auto=validate`, so entities must match the schema exactly.
- **Keep the README in sync.** When a change affects anything the README covers (setup, prerequisites, commands, API endpoints and response shapes, database settings or environment variables), update README.md in the same change. Also update this file when the structure or conventions change.
- **Don't `git add`, commit or push.** Leave changes in the working tree. The user reviews them and commits with their own messages.
- Before finishing a change, run `npm test`, and `npm --prefix frontend run build` for frontend changes.
