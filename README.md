# ![RealWorld Example App using Kotlin and Spring](example-logo.png)

[![Actions](https://github.com/gothinkster/spring-boot-realworld-example-app/workflows/Java%20CI/badge.svg)](https://github.com/gothinkster/spring-boot-realworld-example-app/actions)

> ### Spring boot + MyBatis codebase containing real world examples (CRUD, auth, advanced patterns, etc) that adheres to the [RealWorld](https://github.com/gothinkster/realworld-example-apps) spec and API.

This codebase was created to demonstrate a fully fledged full-stack application built with Spring boot + Mybatis including CRUD operations, authentication, routing, pagination, and more.

For more information on how to this works with other frontends/backends, head over to the [RealWorld](https://github.com/gothinkster/realworld) repo.

# GraphQL API

This application provides a GraphQL-only API using the [Netflix DGS framework](https://github.com/Netflix/dgs-framework). The GraphQL schema is located at `src/main/resources/schema/schema.graphqls` and the visualization looks like below.

![](graphql-schema.png)

## GraphQL Endpoints

The GraphQL API is available at `/graphql` with an interactive GraphiQL interface at `/graphiql`.

### Queries
- `me` - Get current authenticated user
- `article(slug)` - Get article by slug
- `articles(first, after, last, before, tag, authoredBy, favoritedBy)` - List articles with cursor-based pagination
- `feed(first, after, last, before)` - Get feed of articles from followed users
- `profile(username)` - Get user profile
- `tags` - Get all tags

### Mutations
- `createUser(input)` - Register a new user
- `login(email, password)` - Authenticate and get JWT token
- `updateUser(input)` - Update current user profile
- `createArticle(input)` - Create a new article
- `updateArticle(slug, input)` - Update an article
- `deleteArticle(slug)` - Delete an article
- `addComment(slug, body)` - Add comment to article
- `deleteComment(slug, id)` - Delete a comment
- `favoriteArticle(slug)` - Favorite an article
- `unfavoriteArticle(slug)` - Unfavorite an article
- `followUser(username)` - Follow a user
- `unfollowUser(username)` - Unfollow a user

# How it works

The application uses Spring Boot (Web, Mybatis).

* Use the idea of Domain Driven Design to separate the business term and infrastructure term.
* Use MyBatis to implement the [Data Mapper](https://martinfowler.com/eaaCatalog/dataMapper.html) pattern for persistence.
* Use [CQRS](https://martinfowler.com/bliki/CQRS.html) pattern to separate the read model and write model.

And the code is organized as this:

1. `graphql` is the GraphQL layer implemented with Netflix DGS framework
2. `core` is the business model including entities and services
3. `application` is the high-level services for querying the data transfer objects
4. `infrastructure`  contains all the implementation classes as the technique details

# Security

Integration with Spring Security and add other filter for jwt token process.

The secret key is stored in `application.properties`.

# Database

It uses a ~~H2 in-memory database~~ sqlite database (for easy local test without losing test data after every restart), can be changed easily in the `application.properties` for any other database.

## Sample Data & Login Credentials

The application includes seed data with sample users, articles, tags, comments, and social interactions. You can log in with any of these accounts:

| Username | Email | Password |
|----------|-------|----------|
| johndoe | john@example.com | password123 |
| janedoe | jane@example.com | password123 |
| bobsmith | bob@example.com | password123 |

**Seed data includes:**
- 3 users with profiles
- 5 articles on Spring Boot, REST APIs, Microservices, Docker, and Testing
- 7 tags (java, spring-boot, web-development, tutorial, best-practices, microservices, api-design)
- 5 comments on articles
- 6 article favorites
- 4 follow relationships between users

# Getting started

## Backend (Spring Boot)

You'll need Java 11 installed.

    ./gradlew bootRun

**Note**: `bootRun` automatically cleans and recreates the database with seed data on each run to avoid Flyway migration conflicts during development.

To test that it works, open a browser tab at http://localhost:8080/graphiql to access the GraphQL playground.
Alternatively, you can run a GraphQL query:

    curl -X POST http://localhost:8080/graphql \
      -H "Content-Type: application/json" \
      -d '{"query": "{ tags }"}'

## Frontend (Next.js)

You'll need Node.js installed. **Recommended: Node v14-16** (specified in `frontend/.nvmrc`).

If using `nvm`, switch to the correct version:
```bash
cd frontend
nvm use
```

Then install and run:
```bash
npm install
npm run dev
```

The frontend will run on http://localhost:3000 and connect to the backend on port 8080.

**Note**: The `npm run dev` script includes `NODE_OPTIONS=--openssl-legacy-provider` for compatibility with newer Node versions, but Node 14-16 is still recommended for best compatibility.

# Try it out with [Docker](https://www.docker.com/)

You'll need Docker installed.
	
    ./gradlew bootBuildImage --imageName spring-boot-realworld-example-app
    docker run -p 8081:8080 spring-boot-realworld-example-app

# Try it out with a RealWorld frontend

The GraphQL API endpoint is at http://localhost:8080/graphql with an interactive GraphiQL interface at http://localhost:8080/graphiql.

# Run test

The repository contains a lot of test cases to cover both api test and repository test.

    ./gradlew test

# Code format

Use spotless for code format.

    ./gradlew spotlessJavaApply

# Help

Please fork and PR to improve the project.
