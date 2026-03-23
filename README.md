# Reactive Spring Boot 4 ACL Template

Production-ready single-module Maven template implementing a strict **Controller → Service → Repository** architecture on **Spring Boot 4**, **Java 25**, **WebFlux**, **R2DBC PostgreSQL**, **Redis**, **Liquibase**, **reactive Spring Security**, **JWT auth**, **ACL-based authorization**, **Resilience4j**, **Actuator**, **OpenTelemetry**, **Swagger/OpenAPI**, and **multi-stream logging**.

## Technology Decisions
- **Spring Boot 4 starters** for WebFlux, Security, Validation, Actuator, Data R2DBC, Data Redis Reactive, Liquibase, and the **OpenTelemetry starter**.
- **PostgreSQL + R2DBC** for non-blocking persistence and **Redis** for token/ACL caching.
- **springdoc-openapi** for automatic OpenAPI 3 generation and interactive Swagger UI.
- **Logback** with dedicated async appenders for application, business, API, and audit streams.
- **Resilience4j** for circuit breaker, retry, rate limiting, and time limiting.
- **Docker multi-stage build** plus a `compose.yaml` with PostgreSQL, Redis cluster, and LGTM stack.

## Design Decisions
- **Reactive-first execution model**: WebFlux + R2DBC + reactive Redis stay non-blocking end-to-end. Virtual threads remain disabled because they do not improve the event-loop model for this application.
- **ACL-based authorization**: controller-level access plus endpoint/action grants are required; ACLs are cached in Redis and can be refreshed via admin APIs.
- **Redis token control**: access and refresh tokens are stored in Redis so they can be revoked immediately, by user or globally.
- **Upload processing**: the sample multipart API accepts a text/csv file plus metadata, persists valid rows, and returns failed rows with per-line reasons.
- **Container-first local stack**: `compose.yaml` gives a ready-to-run environment with Postgres, Redis cluster, and OTEL LGTM.

## Highlights
- JWT access/refresh token flow with refresh token persistence, Redis-backed revocation, and token-clear APIs.
- ACL authorization with Redis caching plus refresh APIs for one user or all users.
- File upload sample API for CSV/TXT processing with persisted successful rows and failure reporting.
- Liquibase-driven schema management covering users, tokens, ACLs, customers, and uploaded records.
- Swagger UI for interactive endpoint exploration.

## Project Structure
```text
src/main/java/com/example/template
├── acl
├── admin
├── cache
├── config
├── controller
├── dto
├── exception
├── logging
├── mapper
├── repository
│   └── entity
├── security
├── service
│   └── impl
└── upload
```

## API Documentation
- OpenAPI JSON: `GET /v3/api-docs`
- Swagger UI: `GET /swagger-ui.html`

## Admin APIs
- `DELETE /api/v1/admin/tokens/users/{userId}`: clear cached tokens for a user
- `DELETE /api/v1/admin/tokens`: clear all cached tokens
- `POST /api/v1/admin/acl/users/{userId}/refresh`: refresh a user's ACL cache from PostgreSQL
- `POST /api/v1/admin/acl/refresh`: refresh all ACL caches from PostgreSQL

## File Upload API
`POST /api/v1/uploads/records` using `multipart/form-data`:
- part `file`: CSV or TXT file with `name,email` per line
- part `metadata`: JSON like `{"uploadedBy":"ops-user","sourceSystem":"crm"}`

Successful rows are persisted to `processed_upload_record`, and invalid rows are returned with reasons.

## Local Stack
Start everything with:
```bash
docker compose up -d
```
Services:
- PostgreSQL: `localhost:5432`
- Redis cluster: `localhost:6379`
- Grafana LGTM / OTLP: `localhost:3000`, `4317`, `4318`

## Run the app
```bash
mvn spring-boot:run
```

## Run tests with coverage
```bash
mvn verify
```
Coverage report path:
```text
target/site/jacoco/index.html
```
