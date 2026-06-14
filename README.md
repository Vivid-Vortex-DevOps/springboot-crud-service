# springboot-crud-service

A production-style Spring Boot 4.1 REST API demonstrating enterprise Java patterns: JPA with Flyway migrations, OpenTelemetry, Testcontainers-based integration tests, Kubernetes-ready deployment, and GitOps via ArgoCD.

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1 |
| Build | Gradle 8 |
| ORM | Spring Data JPA / Hibernate 7 |
| Database | PostgreSQL 16 |
| Migrations | Flyway 10 |
| Observability | Micrometer + OpenTelemetry bridge |
| Logging | ECS structured JSON (built-in SB 4.1) |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Testing | JUnit 5 + Testcontainers + `@ServiceConnection` |
| Container | Distroless Java 21 non-root image |
| Deployment | Helm + ArgoCD |

## Quick Start

```bash
# Start with Docker Compose (PostgreSQL + app)
docker compose up

# Access the API
curl http://localhost:8080/actuator/health
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Widget","description":"A widget","price":9.99,"stock":100}'

# Swagger UI
open http://localhost:8080/swagger-ui.html
```

## API Summary

| Method | Path | Description |
|---|---|---|
| GET | `/actuator/health` | Health (liveness + readiness) |
| GET | `/actuator/health/liveness` | Kubernetes liveness probe |
| GET | `/actuator/health/readiness` | Kubernetes readiness probe |
| POST | `/api/v1/products` | Create product |
| GET | `/api/v1/products` | List products (paginated) |
| GET | `/api/v1/products/{id}` | Get product |
| PUT | `/api/v1/products/{id}` | Update product |
| DELETE | `/api/v1/products/{id}` | Delete product |
| GET | `/actuator/prometheus` | Prometheus metrics |
| GET | `/swagger-ui.html` | Interactive API docs |

## Repository Structure

```
src/
  main/
    java/com/crud/
      product/
        controller/       — REST endpoints
        service/          — business logic
        repository/       — JPA repository
        entity/           — Product JPA entity
        dto/              — ProductRequest, ProductResponse records
        exception/        — GlobalExceptionHandler, ProductNotFoundException
      config/
        ObservabilityConfig.java  — Micrometer customizer
    resources/
      application.yaml        — base config
      application-dev.yaml    — local dev overrides
      db/migration/           — Flyway SQL migrations
  test/
    java/                     — unit tests (@ExtendWith(MockitoExtension))
    resources/
      application-test.yaml   — test config (H2 in-memory)
  integrationTest/
    java/                     — integration tests (Testcontainers)
deployment/
  helm/                       — Kubernetes Helm chart
  values/
    dev.yaml | qa.yaml | staging.yaml | prod.yaml
Dockerfile                    — multi-stage distroless build
docker-compose.yml            — local development
```

## Running Tests

```bash
# Unit tests
./gradlew test

# Integration tests (Docker required for Testcontainers)
./gradlew integrationTest

# All tests
./gradlew test integrationTest
```

## Deployment

Images are built and pushed to JFrog Artifactory by GitHub Actions on every push to `main`.  
ArgoCD automatically deploys to AKS when `deployment/values/dev.yaml` is updated by CI.

## Key Design Decisions

- **`GenerationType.IDENTITY`** — required for Hibernate 7's changed default strategy
- **`@ServiceConnection`** — Spring Boot 4.1 Testcontainers pattern (replaces `@DynamicPropertySource`)
- **ECS logging** — `logging.structured.format: ecs` in application.yaml (Spring Boot 4.1 built-in)
- **Flyway 10** — requires `flyway-database-postgresql` as a separate artifact
- **`launch.JarLauncher`** — Spring Boot 4.x uses `org.springframework.boot.loader.launch.JarLauncher` (not `loader.JarLauncher` from 3.x)

## Documentation Index

| Topic | Document |
|---|---|
| API Reference | [docs/api.md](docs/api.md) |
| Local Development | [docs/local-development.md](docs/local-development.md) |
| Testing | [docs/testing.md](docs/testing.md) |
| Deployment | [docs/deployment.md](docs/deployment.md) |
| Observability | [docs/observability.md](docs/observability.md) |

## Related Repositories

| Repository | Purpose |
|---|---|
| [cloud-platform-infra](https://github.com/Vivid-Vortex-DevOps/cloud-platform-infra) | Azure infrastructure + GitOps |
| [go-crud-service](https://github.com/Vivid-Vortex-DevOps/go-crud-service) | Go equivalent service |
