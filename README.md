# Infinite Prints — Quick README

Spring Boot REST API with JWT auth, PostgreSQL, and containerized deployment.

## Quick Start (Local)

```bash
cp .env.example .env.local
docker compose -f docker-compose.local.yml up --build
```

App: http://localhost:8080/api

## Production

```bash
docker compose -f docker-compose.prod.yml up -d
```

Provide env vars for DB credentials and JWT secret via environment or secret manager.

## Files Overview

| File | Purpose |
|------|---------|
| `docker-compose.local.yml` | Local dev: Postgres + app with seed data |
| `docker-compose.prod.yml` | Production: expects all secrets from env |
| `src/main/resources/application.properties` | Dev config (DB, JWT defaults) |
| `src/main/resources/application-prod.properties` | Prod config (validate DB, read secrets from env) |
| `src/main/resources/db/seed/local_seed.sql` | Local seed: creates tables + test user |
| `logback-spring.xml` | Logging config (async files, dev/prod profiles) |

## Database Setup

- **Local**: seed file runs automatically via Docker init
- **Production**: external DB script required (no test data, no seeding)

## Key Features

- JWT auth with access/refresh tokens
- BCrypt password hashing
- Global exception handling
- Audit logging (audit_log table)
- Token blacklist (jwt_tokens table)
- Role-based access control (user_roles table)
