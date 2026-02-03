# Tags Microservice

A self-contained microservice extracted from the Spring Boot RealWorld Example App that provides tag management functionality.

## Overview

This microservice handles all tag-related operations for the RealWorld application. It provides a simple REST API to retrieve all available tags used for categorizing articles.

## API Endpoints

### Get All Tags
```
GET /tags
```

**Response:**
```json
{
  "tags": ["java", "spring-boot", "web-development", "tutorial", "best-practices", "microservices", "api-design"]
}
```

## Technology Stack

- **Framework**: Spring Boot 2.6.3
- **Language**: Java 11
- **Database**: SQLite (embedded)
- **ORM**: MyBatis
- **Migration**: Flyway
- **Build Tool**: Gradle

## Running Locally

### Prerequisites
- Java 11 or higher
- Gradle (or use the included Gradle wrapper)

### Build and Run
```bash
./gradlew bootRun
```

The service will start on port 8081 by default.

### Test the API
```bash
curl http://localhost:8081/tags
```

## Docker

### Build the Docker Image
```bash
docker build -t tags-service .
```

### Run with Docker
```bash
docker run -p 8081:8081 tags-service
```

### Run with Docker Compose
```bash
docker-compose up
```

## Configuration

The service can be configured via environment variables or `application.properties`:

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | 8081 | HTTP server port |
| `spring.datasource.url` | jdbc:sqlite:tags.db | Database connection URL |

## Project Structure

```
tags-service/
├── src/
│   ├── main/
│   │   ├── java/io/spring/tagsservice/
│   │   │   ├── api/              # REST controllers
│   │   │   ├── application/      # Application services
│   │   │   ├── core/             # Domain entities
│   │   │   └── infrastructure/   # MyBatis mappers
│   │   └── resources/
│   │       ├── db/migration/     # Flyway migrations
│   │       ├── mapper/           # MyBatis XML mappings
│   │       └── application.properties
│   └── test/
├── Dockerfile
├── docker-compose.yml
└── build.gradle
```

## Integration with Main Application

This microservice can be integrated with the main RealWorld application by:

1. Updating the main application to call this service's `/tags` endpoint
2. Using service discovery (e.g., Eureka, Consul) for dynamic service location
3. Implementing a reverse proxy (e.g., nginx, API Gateway) to route requests
