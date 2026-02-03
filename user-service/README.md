# User Authentication Microservice

A self-contained microservice extracted from the RealWorld Spring Boot application, handling user authentication, profile management, and follow relationships.

## Features

- User registration and login with JWT authentication
- Current user profile retrieval and updates
- User profile viewing
- Follow/unfollow functionality between users
- Health check endpoint

## API Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/users` | Register a new user | No |
| POST | `/users/login` | Login and get JWT token | No |
| GET | `/user` | Get current user profile | Yes |
| PUT | `/user` | Update current user profile | Yes |
| GET | `/profiles/{username}` | Get user profile | No |
| POST | `/profiles/{username}/follow` | Follow a user | Yes |
| DELETE | `/profiles/{username}/follow` | Unfollow a user | Yes |
| GET | `/health` | Health check | No |

## Technology Stack

- Java 11
- Spring Boot 2.6.3
- Spring Security with JWT
- MyBatis for data access
- SQLite database
- Flyway for database migrations

## Running Locally

### Prerequisites
- Java 11 or higher
- Gradle 7.x

### Build and Run

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

The service will start on port 8081.

## Docker

### Build Docker Image

```bash
docker build -t user-service .
```

### Run with Docker

```bash
docker run -p 8081:8081 user-service
```

### Run with Docker Compose

```bash
docker-compose up -d
```

## Configuration

Environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `JWT_SECRET` | Secret key for JWT signing | (configured in application.properties) |
| `JWT_SESSION_TIME` | JWT token expiration in seconds | 86400 (24 hours) |
| `JAVA_OPTS` | JVM options | (none) |

## Database

The service uses SQLite with Flyway migrations. The database file is created automatically on startup.

Tables:
- `users` - User accounts
- `follows` - User follow relationships

## Integration with Other Services

This microservice is designed to be called by other services in the system. Other services should:

1. Share the same JWT secret to validate tokens locally
2. Call this service's API for user profile data when needed
3. Store only user IDs as references (loose coupling)

## Testing

```bash
# Run tests
./gradlew test
```

## Sample Requests

### Register a new user
```bash
curl -X POST http://localhost:8081/users \
  -H "Content-Type: application/json" \
  -d '{"user": {"email": "test@example.com", "username": "testuser", "password": "password123"}}'
```

### Login
```bash
curl -X POST http://localhost:8081/users/login \
  -H "Content-Type: application/json" \
  -d '{"user": {"email": "test@example.com", "password": "password123"}}'
```

### Get current user (authenticated)
```bash
curl http://localhost:8081/user \
  -H "Authorization: Token <your-jwt-token>"
```
