# HMCTS Dev Test Backend

## Overview

This service is a Java 21 / Spring Boot backend for task management.

It exposes REST endpoints for creating, listing, updating, and deleting tasks, with:
- DTO-based API contracts
- validation at the API boundary
- typed domain exceptions and HTTP error mapping
- pagination and sorting support
- persistence with SQLite + JPA
- schema management with Flyway
- test quality gates with JaCoCo thresholds

## Tech Stack

- Java 21
- Spring Boot 3
- Spring Web
- Spring Data JPA
- SQLite
- Flyway
- JUnit 5 + Mockito + Spring Test
- JaCoCo
- Gradle

## API Summary

Base URL: `http://localhost:4000`

### Core Endpoints

- `GET /tasks/{id}`: fetch task by id
- `GET /tasks`: list tasks (paged + sortable)
- `POST /tasks`: create task
- `PATCH /tasks/{id}`: update task
- `DELETE /tasks/{id}`: delete task

### Pagination and Sorting (`GET /tasks`)

Query params:
- `page` (default: `0`)
- `size` (default: `20`, max: `100`)
- `sortBy` (supported: `id`, `title`, `status`, `due_date`)
- `direction` (supported: `asc`, `desc`)

Response shape:
- `items`
- `page`
- `size`
- `totalElements`
- `totalPages`
- `first`
- `last`

## Validation and Error Handling

Validation is enforced on request DTOs (e.g. title/status/due date constraints).

Typed exceptions are mapped to explicit HTTP responses:
- `400 Bad Request`: invalid input / validation
- `404 Not Found`: task not found
- `409 Conflict`: invalid state transition (e.g. reopening completed task)

## Data Model

`Task` includes:
- `id`
- `version` (optimistic locking via `@Version`)
- `title`
- `description`
- `status`
- `dueDate`
- `createdAt`
- `updatedAt`

Concurrency control:
- optimistic locking prevents lost updates on concurrent writes

## Database Migrations

Schema is managed by Flyway migrations in:

- `src/main/resources/db/migration`

Current migrations:
- `V1__create_task_table.sql`
- `V2__seed_initial_tasks.sql`

Notes:
- Hibernate DDL is set to `validate` for runtime safety.
- Flyway is the source of truth for schema evolution.

## Operational Endpoints (Actuator)

The backend exposes a minimal operational surface:

- `GET /actuator/health`
- `GET /actuator/health/liveness`
- `GET /actuator/health/readiness`
- `GET /actuator/info`
- `GET /actuator/metrics`

Readiness includes dependency checks (including DB) before traffic should be routed.

## Local Run

From `backend/`:

```bash
./gradlew bootRun
```

Service runs on:

http://localhost:4000

## Test and Verification

Run full backend quality gate:

```bash
./gradlew clean check
```

Run individual suites:

```bash
./gradlew test
./gradlew integration
./gradlew functional
./gradlew smoke
```

## Coverage Gates

JaCoCo verification is enforced in `check` with minimum thresholds:

- line coverage: `75%`
- branch coverage: `60%`

Build fails if thresholds are not met.

## Security Note

This repository is a public sample and is not currently deployed.

For production deployment:

- actuator endpoints should be restricted using authentication/authorization and/or private network access

- only required management endpoints should be exposed