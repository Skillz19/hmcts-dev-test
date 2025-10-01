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

---

## 🚀 Getting Started

### Prerequisites

- [Docker](https://www.docker.com/) & [Docker Compose](https://docs.docker.com/compose/)
- Java 21+ (only if running backend outside Docker)
- Node.js 20+ & Yarn 3 (only if running frontend outside Docker)

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

### Testing

Backend

cd backend
./gradlew test
