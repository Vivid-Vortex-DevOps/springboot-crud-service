# Local Development

## Prerequisites

- Java 21 (recommend [SDKMAN](https://sdkman.io): `sdk install java 21.0.5-tem`)
- Docker + Docker Compose
- Gradle wrapper is included — no separate Gradle install needed

## Quick Start with Docker Compose

```bash
docker compose up
```

This starts:
- PostgreSQL 16 on port 5432
- Spring Boot app on port 8080

Test:
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/swagger-ui.html   # open in browser
```

## Running Without Docker Compose

```bash
# Start only PostgreSQL
docker compose up -d postgres

# Run application in dev profile
./gradlew bootRun --args='--spring.profiles.active=dev'
```

The `dev` profile (in `application-dev.yaml`) points to `localhost:5432`.

## Environment Variables

| Variable | Required | Default | Description |
|---|---|---|---|
| `SPRING_DATASOURCE_URL` | Yes (K8s) | From compose | JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | Yes (K8s) | `crud_user` | DB username |
| `SPRING_DATASOURCE_PASSWORD` | Yes (K8s) | — | DB password |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | No | `""` | OTLP endpoint (empty = disabled) |
| `SPRING_PROFILES_ACTIVE` | No | `dev` | Active profile |

In Docker Compose, these come from the `environment:` section in `docker-compose.yml`.

## Build

```bash
# Compile only
./gradlew compileJava

# Build JAR (skips tests)
./gradlew bootJar -x test

# Full build with tests
./gradlew build
```

## Running Tests

```bash
# Unit tests (no Docker needed)
./gradlew test

# Integration tests (Docker required for Testcontainers)
./gradlew integrationTest

# All tests with coverage
./gradlew test integrationTest jacocoTestReport
```

Test reports: `build/reports/tests/`

## Project Structure

```
src/main/java/com/crud/
  product/
    controller/ProductController.java    — REST endpoints
    service/ProductService.java          — business logic + transactions
    repository/ProductRepository.java   — JpaRepository interface
    entity/Product.java                 — JPA entity
    dto/
      ProductRequest.java               — input DTO (Jakarta validation)
      ProductResponse.java              — output DTO (immutable record)
    exception/
      ProductNotFoundException.java     — 404 exception
      GlobalExceptionHandler.java       — @RestControllerAdvice
  config/
    ObservabilityConfig.java            — Micrometer common tags

src/main/resources/
  application.yaml                      — base config (all envs)
  application-dev.yaml                  — local dev overrides
  db/migration/
    V1__create_products_table.sql       — Flyway migration
```

## Common Issues

### Port 5432 already in use
Another PostgreSQL is running locally. Either stop it or change the mapped port in `docker-compose.yml`.

### `FlywayException: Found non-empty schema` on first run
Flyway found an existing database. Drop and recreate:
```bash
docker compose down -v  # removes volumes
docker compose up
```

### `ClassNotFoundException: org.springframework.boot.loader.launch.JarLauncher`
This is the Spring Boot 4.x launcher path. If you see this with a Spring Boot 3.x JAR, you have a version mismatch. This project uses Spring Boot 4.1 — ensure you're building with the correct `bootJar`.

### Slow startup in integration tests
Testcontainers downloads the postgres:16-alpine image on first run. Subsequent runs are fast.
