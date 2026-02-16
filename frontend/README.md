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

- Node.js 20+
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

- `http://localhost:3100`

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

## Test Notes

- Unit tests cover extracted route helper logic (query parsing, payload mapping, date formatting).
- Route tests are self-contained and mock backend API calls (no real backend required).

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
