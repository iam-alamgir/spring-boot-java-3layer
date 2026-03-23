# Reactive Spring Boot 4 ACL Template

Production-ready single-module Maven template implementing a strict **Controller → Service → Repository** architecture on **Spring Boot 4**, **Java 25**, **WebFlux**, **R2DBC PostgreSQL**, **Liquibase**, **reactive Spring Security**, **JWT auth**, **ACL-based authorization**, **Resilience4j**, **Actuator**, **OpenTelemetry**, **Swagger/OpenAPI**, and **multi-stream logging**.

## Technology Decisions
- **Spring Boot 4 starters** for the core platform, including WebFlux, Security, Validation, Actuator, Data R2DBC, Liquibase, and the **OpenTelemetry starter** for first-class observability wiring.
- **Java 25** with records, sealed classes, pattern matching, constructor injection, and immutable DTOs.
- **PostgreSQL + R2DBC** for non-blocking persistence.
- **Liquibase** for versioned schema evolution.
- **springdoc-openapi** for automatic OpenAPI 3 generation and interactive Swagger UI.
- **Logback** with dedicated async appenders for application, business, API, and audit streams.
- **Resilience4j** for circuit breaker, retry, rate limiting, and time limiting.
- **MapStruct** for explicit DTO mapping.
- **JaCoCo** to enforce a minimum 90% instruction coverage target during `mvn verify`.

## Design Decisions
- **Reactive-first execution model**: WebFlux + R2DBC stay non-blocking end-to-end. Virtual threads are intentionally disabled because WebFlux already uses an event-loop model; adding virtual threads at request-processing level would weaken the reactive contract rather than improve it.
- **Strict 3-layer architecture**: controllers handle transport/validation, services contain business rules, repositories isolate persistence.
- **ACL-based authorization**: access is granted per controller and per endpoint action instead of broad roles, allowing finer-grained enterprise permission management.
- **DTO isolation**: entities are never exposed directly to API consumers.
- **Centralized error model**: a sealed exception hierarchy produces consistent error responses and routes logs to the appropriate stream.
- **Observability-first template**: Actuator, OTLP export, correlated logs, and generated OpenAPI docs are part of the default project baseline.

## Highlights
- Reactive-first implementation with non-blocking WebFlux and R2DBC.
- Java 25-oriented style using records, sealed exception hierarchy, pattern matching switch, immutable DTOs, and constructor injection.
- ACL authorization with controller-level and endpoint-level grants persisted in PostgreSQL.
- JWT access/refresh token flow with refresh token persistence and revocation.
- API/body logging with masking for sensitive fields and correlation via trace/span IDs.
- Liquibase-driven schema management and seeded example ACL data.
- Swagger UI for interactive endpoint exploration.
- JaCoCo coverage gate configured to enforce **90% instruction coverage** during `mvn verify`.

## Project Structure
```text
src/main/java/com/example/template
├── acl
├── config
├── controller
├── dto
├── exception
├── logging
├── mapper
├── repository
│   └── entity
├── security
└── service
    └── impl
```

## API Documentation
OpenAPI documentation is auto-generated for the controllers.
- OpenAPI JSON: `GET /v3/api-docs`
- Swagger UI: `GET /swagger-ui.html`

## Sample Credentials
The initial Liquibase changelog seeds an `admin` user placeholder. Replace the BCrypt hash before production use.

## Running Locally
### Prerequisites
- Java 25
- Maven 3.9+
- PostgreSQL 16+
- OTLP collector optional for tracing/log export

### Start PostgreSQL
Create database/user matching `application.yml`, or override with environment variables / profile-specific values.

### Run the app
```bash
mvn spring-boot:run
```

### Run tests with coverage
```bash
mvn verify
```
Coverage report will be generated at:
```text
target/site/jacoco/index.html
```

## Authentication Flow
1. `POST /api/v1/auth/login` with username/password.
2. Receive access token + refresh token.
3. Call protected APIs with `Authorization: Bearer <access-token>`.
4. Refresh with `POST /api/v1/auth/refresh`.

## ACL Model
Permissions are stored in `acl_permission` and must include:
- `CONTROLLER / CustomerController / ACCESS`
- `ENDPOINT / CUSTOMER_API / CREATE`
- `ENDPOINT / CUSTOMER_API / READ`

Both controller and endpoint grants are required.

## Logging Streams
- `logs/application.log`: general technical/application events
- `logs/business.log`: business journey events
- `logs/api.log`: request/response payloads and execution time
- `logs/audit.log`: authentication and authorization audit events

## Profiles
- `dev`: default local development
- `test`: H2 + R2DBC H2 for automated tests
- `prod`: connection pool enabled and stricter health details

## Sample API
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"integration-user","password":"secret"}'

curl -X POST http://localhost:8080/api/v1/customers \
  -H "Authorization: Bearer <token>" \
  -H 'Content-Type: application/json' \
  -d '{"fullName":"Jane Roe","email":"jane.roe@example.com"}'
```
