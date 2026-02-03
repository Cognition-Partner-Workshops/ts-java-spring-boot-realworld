# Multi-stage Dockerfile for Spring Boot RealWorld Example App
# Optimized for size and build time

# Stage 1: Build stage
FROM eclipse-temurin:11-jdk-alpine AS builder

WORKDIR /app

# Copy Gradle wrapper and build files first (for better caching)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY src src

# Build the application (skip tests for faster build)
RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: Runtime stage
FROM eclipse-temurin:11-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Create directory for SQLite database with proper permissions
RUN mkdir -p /app/data && chown -R appuser:appgroup /app

# Copy the built JAR from builder stage
COPY --from=builder --chown=appuser:appgroup /app/build/libs/*.jar app.jar

# Switch to non-root user
USER appuser

# Expose the application port
EXPOSE 8080

# Set environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_DATASOURCE_URL="jdbc:sqlite:/app/data/app.db"

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/tags || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
