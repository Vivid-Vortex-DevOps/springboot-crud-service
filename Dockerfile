# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

# Cache Gradle dependencies before copying source
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle settings.gradle .
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon -q

# Build the application (skip tests — tests run in CI before Docker build)
COPY src/ src/
RUN ./gradlew bootJar --no-daemon -x test

# ─── Stage 2: Extract layered JAR ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS extractor

WORKDIR /workspace
COPY --from=builder /workspace/build/libs/*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

# ─── Stage 3: Runtime (distroless — no shell, non-root) ──────────────────────
# Spring Boot 4.x launcher path: org.springframework.boot.loader.launch.JarLauncher
# (differs from 3.x which used org.springframework.boot.loader.JarLauncher)
FROM gcr.io/distroless/java21-debian12:nonroot AS runtime

WORKDIR /app

COPY --from=extractor /workspace/extracted/dependencies/          ./
COPY --from=extractor /workspace/extracted/spring-boot-loader/    ./
COPY --from=extractor /workspace/extracted/snapshot-dependencies/ ./
COPY --from=extractor /workspace/extracted/application/           ./

# nonroot UID 65532 is built into the distroless:nonroot image
USER nonroot:nonroot

EXPOSE 8080

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
