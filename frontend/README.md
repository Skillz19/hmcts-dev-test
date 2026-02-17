# HMCTS Dev Test Frontend

## Overview

This is the Node.js / TypeScript frontend for the task management application.

It uses:

- Express server-side routing
- Nunjucks templates
- GOV.UK Design System styles/components
- Axios to call the backend API

The frontend renders task list/detail/create/edit views and consumes the backend paginated task API.

## Tech Stack

- Node.js 18.x (recommended for this project)
- TypeScript
- Express
- Nunjucks
- GOV.UK Frontend
- Axios
- Jest (unit + route tests)
- CodeceptJS (functional test scaffolding)

## Run Locally

From `frontend/`:

```bash
yarn install
yarn build:prod
yarn start
```

App runs on:

- `https://localhost:3100`

Development mode:

```bash
yarn start:dev
```

## Configuration

Key environment variable:

- `API_BASE_URL`
  Backend base URL used by frontend routes.
  Default local usage expects backend on `http://localhost:4000`.

## Frontend Routes

- `GET /tasks` - list tasks (paged response from backend)
- `GET /tasks/new` - new task form
- `POST /tasks` - create task
- `GET /tasks/:id` - view task
- `GET /tasks/:id/edit` - edit form
- `POST /tasks/:id/edit` - update task
- `POST /tasks/:id/delete` - delete task

## Testing

Run unit tests:

```bash
yarn test:unit
```

Run route tests:

```bash
yarn test:routes
```

Run all frontend tests (as configured):

```bash
yarn test
```

Git hooks (Husky):

- Hooks are configured at repo root in `.husky/`.
- `pre-commit`: runs `cd frontend && yarn lint-staged` when staged files include `frontend/**`.
- `pre-push`: runs frontend tests when pushed changes include `frontend/**`, and backend checks when pushed changes include `backend/**`.

Run functional tests (CodeceptJS):

```bash
nvm use
yarn test:functional
```

## Test Notes

- Unit tests cover extracted route helper logic (query parsing, payload mapping, date formatting).
- Route tests are self-contained and mock backend API calls
- Functional tests are integration-style and require running services:
  - backend: `http://localhost:4000`
  - frontend: `http://localhost:3100` (docker-compose) or `https://localhost:3100` (local dev)
- Use Node 18 for functional tests (see `frontend/.nvmrc`).
- Recommended functional test setup:
  1. `docker-compose up --build`
  2. `cd frontend && nvm use`
  3. `cd frontend && TEST_URL=http://localhost:3100 yarn test:functional`

## Build and Lint

Build assets:

```bash
yarn build
```

Lint:

```bash
yarn lint
```

## Related Docs

- Root project docs: `README.md`
- Backend docs: `backend/README.md`
