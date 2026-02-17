# HMCTS Dev Test

This project is a full-stack application built as part of the HMCTS technical test.  
It consists of:

- **Backend** – a Java 21 / Spring Boot (Gradle) service providing a REST API.
- **Frontend** – a Node.js / TypeScript / Express app using Nunjucks and the GOV.UK Design System.
- **Docker Compose** – runs both backend and frontend together for local development and review.

---

## 📂 Repository Structure

hmcts-dev-test/
│
├── backend/ # Java backend service (Gradle build)
├── frontend/ # Node.js/TypeScript frontend app
├── docker-compose.yml
└── README.md

## Key Engineering Decisions

- DTO-based backend API contract (`TaskRequest` / `TaskResponse`) to keep transport models separate from persistence entities.
- Input validation at API boundary with explicit `400` responses for invalid payloads.
- Typed domain exception mapping for predictable error semantics (`400` / `404` / `409`).
- Paginated and sortable task listing (`GET /tasks`) with explicit response metadata.
- Optimistic locking with `@Version` to prevent lost updates in concurrent write scenarios.
- Flyway-managed schema and seed data migrations (`V1__create_task_table.sql`, `V2__seed_initial_tasks.sql`).
- JaCoCo coverage verification integrated into `check` as a quality gate.

## Operational Endpoints (Actuator)

This project exposes a minimal set of Spring Boot Actuator endpoints for operational visibility:

- `GET /actuator/health`  
  Overall application health status.

- `GET /actuator/health/liveness`  
  Indicates whether the application process is alive.

- `GET /actuator/health/readiness`  
  Indicates whether the application is ready to receive traffic.  
  This includes dependency checks such as database connectivity.

- `GET /actuator/info`  
  Basic application metadata.

- `GET /actuator/metrics`  
  Runtime and application metrics.

Actuator endpoints are intentionally documented in this README for operations and are not part of the Swagger API contract.

### Security Note

This repository is a public sample project and is not currently deployed.  
For production deployment, actuator endpoints should be restricted using authentication/authorization and/or private network access. Only non-sensitive actuator endpoints are exposed by default.

---

## 🚀 Getting Started

### Prerequisites

- [Docker](https://www.docker.com/) & [Docker Compose](https://docs.docker.com/compose/)
- Java 21+ (only if running backend outside Docker)
- Node.js 18.x (see `frontend/.nvmrc`) & Yarn 3 (only if running frontend outside Docker)

---

### Run with Docker (recommended)

To build and start the whole stack:

```bash
docker-compose up --build
```

This will:

Start backend on http://localhost:4000

Start frontend on http://localhost:3100

The frontend proxies API calls to the backend automatically.

### Run locally without Docker (optional)

#### Backend

```bash
cd backend
./gradlew bootRun
```

Runs on http://localhost:4000

#### Frontend

```bash
cd frontend
yarn install
yarn build:prod
yarn start
```

Runs on http://localhost:3100

### Features

Task management:

Create, view, edit, and delete tasks

Stores title, description, status, and due date

GOV.UK Design System styling

Nunjucks templates for frontend rendering

REST API backend with validation

Production-ready Docker setup

### Configuration

Both backend and frontend can be configured via environment variables.

Backend: exposes port 4000 by default

Frontend: exposes port 3100 and expects API_BASE_URL (defaults to backend service in docker-compose)

## Quality Gates

Run backend quality gate:

```bash
cd backend
./gradlew clean check
```

Run frontend route tests:

```bash
cd frontend
yarn test:routes
```

## Git Hooks

This repository uses Husky hooks from the root `.husky/` directory:

- `pre-commit`: runs frontend lint-staged checks when staged files include `frontend/**`.
- `pre-push`: runs frontend tests when pushed changes include `frontend/**`.
- `pre-push`: runs backend `./gradlew check` when pushed changes include `backend/**`.

## Documentation Map

- Backend deep dive: `backend/README.md`
- Frontend deep dive: `frontend/README.md`
