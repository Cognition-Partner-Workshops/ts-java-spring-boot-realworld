# Multi-stage Dockerfile for Spring Boot RealWorld Example App
# Microservice Use Case: Article Management Service
# This application demonstrates a production-grade blogging platform API

# ============================================
# Stage 1: Build Stage
# Using Java 17 (latest LTS compatible with Spring Boot 2.6.x)
# ============================================
FROM eclipse-temurin:17-jdk-alpine AS builder

# Set working directory
WORKDIR /app

# Install required build tools
RUN apk add --no-cache bash

# Copy Gradle wrapper and build files first for better layer caching
COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle build.gradle

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY src src

# Build the application (skip tests for faster builds, tests run in CI)
RUN ./gradlew bootJar --no-daemon -x test

# ============================================
# Stage 2: Runtime Stage
# Using Java 17 JRE (latest LTS compatible with Spring Boot 2.6.x)
# ============================================
FROM eclipse-temurin:17-jre-alpine AS runtime

# Labels for container metadata
LABEL maintainer="Devin AI"
LABEL description="Spring Boot RealWorld Example App - Article Management Microservice"
LABEL version="1.0.0"

# Create non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Set working directory
WORKDIR /app

# Create directory for SQLite database with proper permissions
RUN mkdir -p /app/data && chown -R appuser:appgroup /app

# Copy the built JAR from builder stage
COPY --from=builder --chown=appuser:appgroup /app/build/libs/*.jar app.jar

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8080

# Health check endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/tags || exit 1

# JVM options for container environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
