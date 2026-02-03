# User Authentication Microservice

A self-contained microservice extracted from the RealWorld Example App that handles user authentication, registration, profile management, and follow relationships.

## Features

- **User Registration** (`POST /users`) - Create new user accounts
- **User Login** (`POST /users/login`) - Authenticate users and receive JWT tokens
- **Current User** (`GET /user`, `PUT /user`) - Get and update the authenticated user's profile
- **Profiles** (`GET /profiles/{username}`) - View user profiles
- **Follow/Unfollow** (`POST/DELETE /profiles/{username}/follow`) - Manage follow relationships
- **Health Check** (`GET /health`) - Service health endpoint

## Technology Stack

- **Framework**: Spring Boot 2.6.3
- **Security**: Spring Security with JWT (HS512)
- **Database**: SQLite (can be replaced with PostgreSQL/MySQL for production)
- **ORM**: MyBatis
- **Build**: Gradle
- **Container**: Docker

## API Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/users` | Register a new user | No |
| POST | `/users/login` | Login and get JWT token | No |
| GET | `/user` | Get current user | Yes |
| PUT | `/user` | Update current user | Yes |
| GET | `/profiles/{username}` | Get user profile | No |
| POST | `/profiles/{username}/follow` | Follow a user | Yes |
| DELETE | `/profiles/{username}/follow` | Unfollow a user | Yes |
| GET | `/health` | Health check | No |

## Running Locally

### Prerequisites
- Java 11+
- Gradle 7+

### Start the service
```bash
./gradlew bootRun
```

The service will start on port 8081 by default.

### Run tests
```bash
./gradlew test
```

## Running with Docker

### Build the image
```bash
docker build -t user-auth-service .
```

### Run with Docker Compose
```bash
docker-compose up
```

The service will be available at `http://localhost:8081`.

## Configuration

Key configuration properties in `application.properties`:

| Property | Description | Default |
|----------|-------------|---------|
| `server.port` | Server port | 8081 |
| `jwt.secret` | JWT signing secret | (configured) |
| `jwt.sessionTime` | JWT expiration in seconds | 86400 (24 hours) |
| `spring.datasource.url` | Database connection URL | jdbc:sqlite:user-auth.db |

## Integration with Main Application

This microservice can be integrated with the main RealWorld application by:

1. Configuring the main app to call this service for authentication
2. Sharing the same JWT secret between services
3. Using the `/health` endpoint for service discovery and health checks

## Architecture

```
user-auth-service/
├── src/main/java/io/spring/realworld/userauth/
│   ├── api/                    # REST controllers
│   │   ├── exception/          # Exception handlers
│   │   └── security/           # JWT filter and security config
│   ├── application/            # Application services
│   │   ├── data/               # DTOs
│   │   └── user/               # User service and validators
│   ├── core/                   # Domain layer
│   │   ├── service/            # Service interfaces
│   │   └── user/               # User entity and repository
│   └── infrastructure/         # Infrastructure layer
│       ├── mybatis/            # MyBatis mappers
│       ├── repository/         # Repository implementations
│       └── service/            # Service implementations
└── src/main/resources/
    ├── db/migration/           # Flyway migrations
    └── mapper/                 # MyBatis XML mappers
```
