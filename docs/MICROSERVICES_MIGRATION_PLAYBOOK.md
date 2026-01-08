# Java Spring Modular Monolith to Spring Boot Microservices on AWS

## Playbook for Migration with Gradle CI

This playbook provides a comprehensive guide for migrating a Java Spring modular monolith application to Spring Boot microservices deployed on AWS compute services, using Gradle for continuous integration and deployment.

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Assessment and Planning](#assessment-and-planning)
3. [Decomposition Strategy](#decomposition-strategy)
4. [Spring Boot Microservices Architecture](#spring-boot-microservices-architecture)
5. [AWS Compute Options](#aws-compute-options)
6. [Gradle CI/CD Pipeline](#gradle-cicd-pipeline)
7. [Inter-Service Communication](#inter-service-communication)
8. [Data Management Strategy](#data-management-strategy)
9. [Security Implementation](#security-implementation)
10. [Monitoring and Observability](#monitoring-and-observability)
11. [Testing Strategy](#testing-strategy)
12. [Deployment Strategy](#deployment-strategy)
13. [Migration Checklist](#migration-checklist)

---

## Executive Summary

This playbook guides teams through the process of decomposing a modular monolith Spring application into independent microservices. The migration leverages Spring Boot's production-ready features, AWS compute services for scalable deployment, and Gradle for automated build and deployment pipelines.

The approach follows Domain-Driven Design (DDD) principles to identify bounded contexts and uses the Strangler Fig pattern for incremental migration, minimizing risk while maintaining system availability.

---

## Assessment and Planning

### Phase 1: Current State Analysis

Before beginning migration, conduct a thorough assessment of the existing modular monolith.

#### Architecture Review

Analyze the current application structure. A well-designed modular monolith typically follows this pattern:

```
src/main/java/com/example/
├── api/                    # REST controllers (web layer)
├── core/                   # Domain entities and repository interfaces
│   ├── article/
│   ├── user/
│   ├── comment/
│   └── favorite/
├── application/            # Query/Command services (CQRS pattern)
├── infrastructure/         # Database implementations
└── graphql/               # GraphQL data fetchers (if applicable)
```

#### Dependency Mapping

Create a dependency matrix showing how modules interact:

| Module | Depends On | Depended By | Database Tables | External APIs |
|--------|------------|-------------|-----------------|---------------|
| User | - | Article, Comment, Favorite | users, follows | Auth Provider |
| Article | User | Comment, Favorite | articles, tags, article_tags | - |
| Comment | User, Article | - | comments | - |
| Favorite | User, Article | - | article_favorites | - |

#### Technical Debt Assessment

Identify and document technical debt that should be addressed during migration, including deprecated APIs, outdated dependencies, missing tests, and performance bottlenecks.

### Phase 2: Migration Planning

#### Define Success Criteria

Establish clear, measurable goals for the migration including target response times (p99 latency), availability requirements (99.9% uptime), deployment frequency targets, and rollback time objectives.

#### Risk Assessment

Identify potential risks and mitigation strategies:

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| Data inconsistency | High | Medium | Implement saga pattern, eventual consistency |
| Service communication failures | High | Medium | Circuit breakers, retry policies |
| Increased operational complexity | Medium | High | Comprehensive monitoring, runbooks |
| Performance degradation | High | Low | Load testing, caching strategies |

---

## Decomposition Strategy

### Identifying Bounded Contexts

Use Domain-Driven Design to identify natural service boundaries. Each bounded context becomes a candidate microservice.

#### Example: RealWorld Application Decomposition

Based on the modular monolith structure, identify these bounded contexts:

**User Service** handles user registration and authentication, profile management, user following relationships, and JWT token generation and validation.

**Article Service** manages article CRUD operations, tag management, slug generation, and article search and filtering.

**Social Service** handles comments on articles, article favoriting, and feed generation.

### Service Boundary Guidelines

When defining service boundaries, ensure each service owns its data completely, services communicate through well-defined APIs, avoid circular dependencies between services, and each service can be deployed independently.

### Strangler Fig Pattern Implementation

Migrate incrementally using the Strangler Fig pattern:

```
Phase 1: Extract User Service
┌─────────────────────────────────────────┐
│              API Gateway                │
│    ┌─────────────┬─────────────────┐   │
│    │ /users/*    │ /articles/*     │   │
│    │ /profiles/* │ /tags           │   │
│    └──────┬──────┴────────┬────────┘   │
│           │               │            │
│           v               v            │
│    ┌──────────┐    ┌──────────────┐   │
│    │  User    │    │   Monolith   │   │
│    │ Service  │    │  (remaining) │   │
│    └──────────┘    └──────────────┘   │
└─────────────────────────────────────────┘
```

---

## Spring Boot Microservices Architecture

### Project Structure

Each microservice follows a consistent structure:

```
user-service/
├── build.gradle
├── settings.gradle
├── src/
│   ├── main/
│   │   ├── java/com/example/user/
│   │   │   ├── UserServiceApplication.java
│   │   │   ├── api/
│   │   │   │   ├── UserController.java
│   │   │   │   └── ProfileController.java
│   │   │   ├── domain/
│   │   │   │   ├── User.java
│   │   │   │   ├── FollowRelation.java
│   │   │   │   └── UserRepository.java
│   │   │   ├── application/
│   │   │   │   ├── UserService.java
│   │   │   │   └── UserQueryService.java
│   │   │   ├── infrastructure/
│   │   │   │   ├── persistence/
│   │   │   │   └── security/
│   │   │   └── config/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── application-prod.yml
│   └── test/
├── Dockerfile
└── docker-compose.yml
```

### Gradle Multi-Project Build

Create a parent project to manage all microservices:

```groovy
// settings.gradle (root)
rootProject.name = 'realworld-microservices'

include 'user-service'
include 'article-service'
include 'social-service'
include 'api-gateway'
include 'common-lib'
```

```groovy
// build.gradle (root)
plugins {
    id 'org.springframework.boot' version '3.2.0' apply false
    id 'io.spring.dependency-management' version '1.1.4' apply false
    id 'java'
}

subprojects {
    apply plugin: 'java'
    apply plugin: 'org.springframework.boot'
    apply plugin: 'io.spring.dependency-management'

    group = 'com.example.realworld'
    version = '1.0.0-SNAPSHOT'

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation 'org.springframework.boot:spring-boot-starter-web'
        implementation 'org.springframework.boot:spring-boot-starter-actuator'
        implementation 'org.springframework.boot:spring-boot-starter-validation'
        
        compileOnly 'org.projectlombok:lombok'
        annotationProcessor 'org.projectlombok:lombok'
        
        testImplementation 'org.springframework.boot:spring-boot-starter-test'
    }

    test {
        useJUnitPlatform()
    }
}
```

### Individual Service Build Configuration

```groovy
// user-service/build.gradle
plugins {
    id 'org.springframework.boot'
    id 'io.spring.dependency-management'
    id 'java'
}

dependencies {
    implementation project(':common-lib')
    
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.springframework.cloud:spring-cloud-starter-netflix-eureka-client'
    
    implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'
    
    runtimeOnly 'org.postgresql:postgresql'
    
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
    testImplementation 'org.testcontainers:postgresql'
    testImplementation 'org.testcontainers:junit-jupiter'
}

dependencyManagement {
    imports {
        mavenBom "org.springframework.cloud:spring-cloud-dependencies:2023.0.0"
    }
}

bootJar {
    archiveFileName = 'user-service.jar'
}
```

### Application Configuration

```yaml
# user-service/src/main/resources/application.yml
spring:
  application:
    name: user-service
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/userdb}
    username: ${DATABASE_USERNAME:postgres}
    password: ${DATABASE_PASSWORD:postgres}
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

server:
  port: ${SERVER_PORT:8081}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always

eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_SERVER:http://localhost:8761/eureka}
  instance:
    prefer-ip-address: true

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000
```

---

## AWS Compute Options

### Option 1: Amazon ECS with Fargate (Recommended for Most Cases)

Amazon ECS with Fargate provides serverless container orchestration, eliminating the need to manage underlying infrastructure.

#### Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        AWS Cloud                            │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                    VPC                               │   │
│  │  ┌─────────────────────────────────────────────┐    │   │
│  │  │              Application Load Balancer       │    │   │
│  │  └─────────────────────────────────────────────┘    │   │
│  │                         │                           │   │
│  │  ┌──────────────────────┼──────────────────────┐   │   │
│  │  │              ECS Cluster (Fargate)          │   │   │
│  │  │  ┌─────────┐  ┌─────────┐  ┌─────────┐     │   │   │
│  │  │  │  User   │  │ Article │  │ Social  │     │   │   │
│  │  │  │ Service │  │ Service │  │ Service │     │   │   │
│  │  │  └─────────┘  └─────────┘  └─────────┘     │   │   │
│  │  └────────────────────────────────────────────┘   │   │
│  │                         │                           │   │
│  │  ┌──────────────────────┼──────────────────────┐   │   │
│  │  │              Amazon RDS                      │   │   │
│  │  │  ┌─────────┐  ┌─────────┐  ┌─────────┐     │   │   │
│  │  │  │ UserDB  │  │ArticleDB│  │SocialDB │     │   │   │
│  │  │  └─────────┘  └─────────┘  └─────────┘     │   │   │
│  │  └────────────────────────────────────────────┘   │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

#### ECS Task Definition

```json
{
  "family": "user-service",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "executionRoleArn": "arn:aws:iam::ACCOUNT_ID:role/ecsTaskExecutionRole",
  "taskRoleArn": "arn:aws:iam::ACCOUNT_ID:role/ecsTaskRole",
  "containerDefinitions": [
    {
      "name": "user-service",
      "image": "ACCOUNT_ID.dkr.ecr.REGION.amazonaws.com/user-service:latest",
      "portMappings": [
        {
          "containerPort": 8081,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "prod"
        }
      ],
      "secrets": [
        {
          "name": "DATABASE_URL",
          "valueFrom": "arn:aws:secretsmanager:REGION:ACCOUNT_ID:secret:user-service/database-url"
        },
        {
          "name": "JWT_SECRET",
          "valueFrom": "arn:aws:secretsmanager:REGION:ACCOUNT_ID:secret:jwt-secret"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/user-service",
          "awslogs-region": "us-east-1",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command": ["CMD-SHELL", "curl -f http://localhost:8081/actuator/health || exit 1"],
        "interval": 30,
        "timeout": 5,
        "retries": 3,
        "startPeriod": 60
      }
    }
  ]
}
```

### Option 2: Amazon EKS (For Complex Orchestration Needs)

Amazon EKS provides managed Kubernetes for teams requiring advanced orchestration features.

#### Kubernetes Deployment

```yaml
# user-service-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
  namespace: realworld
spec:
  replicas: 3
  selector:
    matchLabels:
      app: user-service
  template:
    metadata:
      labels:
        app: user-service
    spec:
      containers:
      - name: user-service
        image: ACCOUNT_ID.dkr.ecr.REGION.amazonaws.com/user-service:latest
        ports:
        - containerPort: 8081
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: user-service-secrets
              key: database-url
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8081
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: user-service
  namespace: realworld
spec:
  selector:
    app: user-service
  ports:
  - port: 80
    targetPort: 8081
  type: ClusterIP
```

### Option 3: AWS Lambda (For Event-Driven Workloads)

For specific microservices with sporadic traffic patterns, AWS Lambda can be cost-effective.

```groovy
// build.gradle for Lambda deployment
plugins {
    id 'org.springframework.boot'
    id 'io.spring.dependency-management'
    id 'java'
}

dependencies {
    implementation 'org.springframework.cloud:spring-cloud-function-adapter-aws'
    implementation 'com.amazonaws:aws-lambda-java-events:3.11.0'
    implementation 'com.amazonaws:aws-lambda-java-core:1.2.2'
}

task buildZip(type: Zip) {
    from compileJava
    from processResources
    into('lib') {
        from configurations.runtimeClasspath
    }
}
```

### Compute Option Comparison

| Criteria | ECS Fargate | EKS | Lambda |
|----------|-------------|-----|--------|
| Operational Overhead | Low | Medium-High | Very Low |
| Cost (steady traffic) | Medium | Medium | High |
| Cost (sporadic traffic) | Medium | Medium | Low |
| Scaling Speed | Seconds | Seconds | Milliseconds |
| Cold Start | None | None | Yes (mitigated with SnapStart) |
| Max Execution Time | Unlimited | Unlimited | 15 minutes |
| Best For | Most microservices | Complex orchestration | Event-driven, sporadic |

---

## Gradle CI/CD Pipeline

### GitHub Actions Workflow

```yaml
# .github/workflows/ci-cd.yml
name: CI/CD Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

env:
  AWS_REGION: us-east-1
  ECR_REGISTRY: ${{ secrets.AWS_ACCOUNT_ID }}.dkr.ecr.us-east-1.amazonaws.com

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        service: [user-service, article-service, social-service]
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        distribution: 'temurin'
        java-version: '21'
        cache: 'gradle'
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Build and Test
      run: ./gradlew :${{ matrix.service }}:build :${{ matrix.service }}:test
    
    - name: Run Integration Tests
      run: ./gradlew :${{ matrix.service }}:integrationTest
    
    - name: Generate Test Report
      uses: dorny/test-reporter@v1
      if: always()
      with:
        name: Test Results - ${{ matrix.service }}
        path: ${{ matrix.service }}/build/test-results/test/*.xml
        reporter: java-junit
    
    - name: Upload Build Artifacts
      uses: actions/upload-artifact@v4
      with:
        name: ${{ matrix.service }}-jar
        path: ${{ matrix.service }}/build/libs/*.jar

  code-quality:
    runs-on: ubuntu-latest
    needs: build-and-test
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 21
      uses: actions/setup-java@v4
      with:
        distribution: 'temurin'
        java-version: '21'
        cache: 'gradle'
    
    - name: Run Spotless Check
      run: ./gradlew spotlessCheck
    
    - name: Run SpotBugs
      run: ./gradlew spotbugsMain
    
    - name: Run Checkstyle
      run: ./gradlew checkstyleMain

  security-scan:
    runs-on: ubuntu-latest
    needs: build-and-test
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Run OWASP Dependency Check
      run: ./gradlew dependencyCheckAnalyze
    
    - name: Upload Dependency Check Report
      uses: actions/upload-artifact@v4
      with:
        name: dependency-check-report
        path: build/reports/dependency-check-report.html

  build-and-push-images:
    runs-on: ubuntu-latest
    needs: [code-quality, security-scan]
    if: github.ref == 'refs/heads/main'
    strategy:
      matrix:
        service: [user-service, article-service, social-service]
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Configure AWS credentials
      uses: aws-actions/configure-aws-credentials@v4
      with:
        aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
        aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
        aws-region: ${{ env.AWS_REGION }}
    
    - name: Login to Amazon ECR
      id: login-ecr
      uses: aws-actions/amazon-ecr-login@v2
    
    - name: Download Build Artifact
      uses: actions/download-artifact@v4
      with:
        name: ${{ matrix.service }}-jar
        path: ${{ matrix.service }}/build/libs/
    
    - name: Build and Push Docker Image
      env:
        IMAGE_TAG: ${{ github.sha }}
      run: |
        docker build -t $ECR_REGISTRY/${{ matrix.service }}:$IMAGE_TAG \
                     -t $ECR_REGISTRY/${{ matrix.service }}:latest \
                     -f ${{ matrix.service }}/Dockerfile \
                     ${{ matrix.service }}
        docker push $ECR_REGISTRY/${{ matrix.service }}:$IMAGE_TAG
        docker push $ECR_REGISTRY/${{ matrix.service }}:latest

  deploy-to-staging:
    runs-on: ubuntu-latest
    needs: build-and-push-images
    environment: staging
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Configure AWS credentials
      uses: aws-actions/configure-aws-credentials@v4
      with:
        aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
        aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
        aws-region: ${{ env.AWS_REGION }}
    
    - name: Deploy to ECS Staging
      run: |
        for service in user-service article-service social-service; do
          aws ecs update-service \
            --cluster realworld-staging \
            --service $service \
            --force-new-deployment
        done
    
    - name: Wait for Deployment
      run: |
        for service in user-service article-service social-service; do
          aws ecs wait services-stable \
            --cluster realworld-staging \
            --services $service
        done

  integration-tests-staging:
    runs-on: ubuntu-latest
    needs: deploy-to-staging
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Run E2E Tests
      run: ./gradlew e2eTest -Penv=staging
    
    - name: Run Performance Tests
      run: ./gradlew gatlingRun -Penv=staging

  deploy-to-production:
    runs-on: ubuntu-latest
    needs: integration-tests-staging
    environment: production
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Configure AWS credentials
      uses: aws-actions/configure-aws-credentials@v4
      with:
        aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
        aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
        aws-region: ${{ env.AWS_REGION }}
    
    - name: Blue-Green Deployment
      run: |
        # Update task definitions with new image
        for service in user-service article-service social-service; do
          # Register new task definition
          NEW_TASK_DEF=$(aws ecs register-task-definition \
            --cli-input-json file://ecs/$service-task-def.json \
            --query 'taskDefinition.taskDefinitionArn' \
            --output text)
          
          # Update service with new task definition
          aws ecs update-service \
            --cluster realworld-production \
            --service $service \
            --task-definition $NEW_TASK_DEF \
            --deployment-configuration "maximumPercent=200,minimumHealthyPercent=100"
        done
    
    - name: Wait for Production Deployment
      run: |
        for service in user-service article-service social-service; do
          aws ecs wait services-stable \
            --cluster realworld-production \
            --services $service
        done
```

### Gradle Build Configuration for CI

```groovy
// build.gradle (root) - CI/CD specific configurations
plugins {
    id 'org.springframework.boot' version '3.2.0' apply false
    id 'io.spring.dependency-management' version '1.1.4' apply false
    id 'com.diffplug.spotless' version '6.23.0'
    id 'com.github.spotbugs' version '6.0.0'
    id 'checkstyle'
    id 'org.owasp.dependencycheck' version '9.0.0'
    id 'io.gatling.gradle' version '3.10.0'
    id 'java'
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply plugin: 'java'
    apply plugin: 'com.diffplug.spotless'
    apply plugin: 'com.github.spotbugs'
    apply plugin: 'checkstyle'

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    spotless {
        java {
            target project.fileTree(project.rootDir) {
                include '**/*.java'
                exclude 'build/**'
            }
            googleJavaFormat('1.18.1')
            removeUnusedImports()
            trimTrailingWhitespace()
            endWithNewline()
        }
    }

    spotbugs {
        ignoreFailures = false
        showStackTraces = true
        showProgress = true
        effort = 'max'
        reportLevel = 'medium'
    }

    checkstyle {
        toolVersion = '10.12.0'
        configFile = rootProject.file('config/checkstyle/checkstyle.xml')
        maxWarnings = 0
    }

    tasks.withType(Test) {
        useJUnitPlatform()
        testLogging {
            events "passed", "skipped", "failed"
            exceptionFormat "full"
        }
    }

    // Integration test configuration
    sourceSets {
        integrationTest {
            java.srcDir 'src/integrationTest/java'
            resources.srcDir 'src/integrationTest/resources'
            compileClasspath += sourceSets.main.output + configurations.testRuntimeClasspath
            runtimeClasspath += output + compileClasspath
        }
    }

    task integrationTest(type: Test) {
        description = 'Runs integration tests.'
        group = 'verification'
        testClassesDirs = sourceSets.integrationTest.output.classesDirs
        classpath = sourceSets.integrationTest.runtimeClasspath
        shouldRunAfter test
    }

    check.dependsOn integrationTest
}

// OWASP Dependency Check configuration
dependencyCheck {
    failBuildOnCVSS = 7
    suppressionFile = 'config/owasp/suppressions.xml'
    analyzers {
        assemblyEnabled = false
    }
}
```

### Dockerfile Template

```dockerfile
# Dockerfile for each microservice
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

# Create non-root user
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Copy application JAR
COPY build/libs/*.jar app.jar

# Set ownership
RUN chown -R appuser:appgroup /app

USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# JVM options for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

---

## Inter-Service Communication

### Synchronous Communication (REST/gRPC)

#### Using Spring Cloud OpenFeign

```java
// In article-service, calling user-service
@FeignClient(name = "user-service", fallback = UserClientFallback.class)
public interface UserClient {
    
    @GetMapping("/api/users/{userId}")
    UserResponse getUserById(@PathVariable String userId);
    
    @GetMapping("/api/profiles/{username}")
    ProfileResponse getProfile(@PathVariable String username);
}

@Component
public class UserClientFallback implements UserClient {
    
    @Override
    public UserResponse getUserById(String userId) {
        return UserResponse.builder()
            .id(userId)
            .username("Unknown")
            .build();
    }
    
    @Override
    public ProfileResponse getProfile(String username) {
        return ProfileResponse.builder()
            .username(username)
            .bio("Profile unavailable")
            .build();
    }
}
```

#### Circuit Breaker Configuration

```yaml
# application.yml
resilience4j:
  circuitbreaker:
    instances:
      user-service:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
        waitDurationInOpenState: 5s
        failureRateThreshold: 50
        eventConsumerBufferSize: 10
  
  retry:
    instances:
      user-service:
        maxAttempts: 3
        waitDuration: 500ms
        enableExponentialBackoff: true
        exponentialBackoffMultiplier: 2
```

### Asynchronous Communication (Event-Driven)

#### Using Amazon SQS/SNS

```java
// Event Publisher
@Service
@RequiredArgsConstructor
public class ArticleEventPublisher {
    
    private final SnsTemplate snsTemplate;
    
    public void publishArticleCreated(Article article) {
        ArticleCreatedEvent event = ArticleCreatedEvent.builder()
            .articleId(article.getId())
            .authorId(article.getUserId())
            .title(article.getTitle())
            .slug(article.getSlug())
            .createdAt(article.getCreatedAt())
            .build();
        
        snsTemplate.sendNotification("article-events", event, null);
    }
}

// Event Consumer
@Service
@RequiredArgsConstructor
public class ArticleEventConsumer {
    
    private final FeedService feedService;
    
    @SqsListener("article-created-queue")
    public void handleArticleCreated(ArticleCreatedEvent event) {
        feedService.addToFollowerFeeds(event.getAuthorId(), event.getArticleId());
    }
}
```

#### Event Schema

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleCreatedEvent {
    private String eventId;
    private String eventType = "ARTICLE_CREATED";
    private Instant timestamp;
    private String articleId;
    private String authorId;
    private String title;
    private String slug;
    private Instant createdAt;
}
```

### API Gateway Pattern

```yaml
# AWS API Gateway configuration (OpenAPI)
openapi: 3.0.0
info:
  title: RealWorld API Gateway
  version: 1.0.0

paths:
  /api/users:
    post:
      x-amazon-apigateway-integration:
        type: http_proxy
        uri: http://user-service.internal/api/users
        httpMethod: POST
  
  /api/articles:
    get:
      x-amazon-apigateway-integration:
        type: http_proxy
        uri: http://article-service.internal/api/articles
        httpMethod: GET
    post:
      x-amazon-apigateway-integration:
        type: http_proxy
        uri: http://article-service.internal/api/articles
        httpMethod: POST
        
  /api/articles/{slug}/comments:
    get:
      x-amazon-apigateway-integration:
        type: http_proxy
        uri: http://social-service.internal/api/articles/{slug}/comments
        httpMethod: GET
```

---

## Data Management Strategy

### Database per Service Pattern

Each microservice owns its database, ensuring loose coupling and independent scalability.

```
┌─────────────────────────────────────────────────────────┐
│                    Microservices                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐    │
│  │    User     │  │   Article   │  │   Social    │    │
│  │   Service   │  │   Service   │  │   Service   │    │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘    │
│         │                │                │            │
│  ┌──────▼──────┐  ┌──────▼──────┐  ┌──────▼──────┐    │
│  │   UserDB    │  │  ArticleDB  │  │  SocialDB   │    │
│  │ (PostgreSQL)│  │ (PostgreSQL)│  │ (PostgreSQL)│    │
│  │             │  │             │  │             │    │
│  │ - users     │  │ - articles  │  │ - comments  │    │
│  │ - follows   │  │ - tags      │  │ - favorites │    │
│  └─────────────┘  └─────────────┘  └─────────────┘    │
└─────────────────────────────────────────────────────────┘
```

### Data Migration Strategy

#### Phase 1: Schema Extraction

```sql
-- user-service schema
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    bio TEXT,
    image VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE follows (
    follower_id UUID REFERENCES users(id),
    followed_id UUID REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (follower_id, followed_id)
);
```

#### Phase 2: Data Synchronization During Migration

```java
// Dual-write pattern during migration
@Service
@RequiredArgsConstructor
public class UserMigrationService {
    
    private final LegacyUserRepository legacyRepo;
    private final NewUserRepository newRepo;
    private final FeatureFlags featureFlags;
    
    @Transactional
    public User createUser(CreateUserRequest request) {
        User user = User.create(request);
        
        // Write to legacy database
        legacyRepo.save(user);
        
        // Write to new database
        if (featureFlags.isEnabled("dual-write-users")) {
            newRepo.save(user);
        }
        
        return user;
    }
    
    public User getUser(String userId) {
        // Read from new database if migration complete
        if (featureFlags.isEnabled("read-from-new-user-db")) {
            return newRepo.findById(userId)
                .orElseGet(() -> legacyRepo.findById(userId).orElse(null));
        }
        return legacyRepo.findById(userId).orElse(null);
    }
}
```

### Saga Pattern for Distributed Transactions

```java
// Orchestration-based Saga for Article Creation
@Service
@RequiredArgsConstructor
public class CreateArticleSaga {
    
    private final ArticleRepository articleRepository;
    private final UserClient userClient;
    private final NotificationClient notificationClient;
    private final SagaOrchestrator sagaOrchestrator;
    
    public Article execute(CreateArticleRequest request) {
        Saga saga = sagaOrchestrator.create("create-article");
        
        try {
            // Step 1: Validate user exists
            saga.step("validate-user", () -> {
                UserResponse user = userClient.getUserById(request.getUserId());
                if (user == null) {
                    throw new UserNotFoundException(request.getUserId());
                }
            });
            
            // Step 2: Create article
            Article article = saga.step("create-article", () -> {
                Article newArticle = Article.create(request);
                return articleRepository.save(newArticle);
            }, article -> articleRepository.delete(article)); // Compensation
            
            // Step 3: Notify followers
            saga.step("notify-followers", () -> {
                notificationClient.notifyFollowers(article.getUserId(), article.getId());
            });
            
            saga.complete();
            return article;
            
        } catch (Exception e) {
            saga.compensate();
            throw new SagaExecutionException("Failed to create article", e);
        }
    }
}
```

---

## Security Implementation

### JWT Authentication Across Services

```java
// Shared JWT validation in common-lib
@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    public String generateToken(UserPrincipal userPrincipal) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        return Jwts.builder()
            .setSubject(userPrincipal.getId())
            .claim("username", userPrincipal.getUsername())
            .claim("email", userPrincipal.getEmail())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact();
    }
    
    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
        
        return claims.getSubject();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

### Service-to-Service Authentication

```java
// Service account authentication for internal calls
@Configuration
public class ServiceAuthConfig {
    
    @Bean
    public RequestInterceptor serviceAuthInterceptor() {
        return requestTemplate -> {
            String serviceToken = generateServiceToken();
            requestTemplate.header("X-Service-Auth", serviceToken);
        };
    }
    
    private String generateServiceToken() {
        return Jwts.builder()
            .setSubject("article-service")
            .claim("type", "service")
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 300000)) // 5 minutes
            .signWith(getServiceSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }
}
```

### AWS Secrets Manager Integration

```java
@Configuration
public class SecretsManagerConfig {
    
    @Bean
    public SecretsManagerClient secretsManagerClient() {
        return SecretsManagerClient.builder()
            .region(Region.US_EAST_1)
            .build();
    }
    
    @Bean
    public String jwtSecret(SecretsManagerClient client) {
        GetSecretValueRequest request = GetSecretValueRequest.builder()
            .secretId("realworld/jwt-secret")
            .build();
        
        GetSecretValueResponse response = client.getSecretValue(request);
        return response.secretString();
    }
}
```

---

## Monitoring and Observability

### Distributed Tracing with AWS X-Ray

```java
// X-Ray configuration
@Configuration
@EnableAspectJAutoProxy
public class XRayConfig {
    
    @Bean
    public AWSXRayServletFilter tracingFilter() {
        return new AWSXRayServletFilter("user-service");
    }
    
    @Bean
    public RestTemplate xrayRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(
            Collections.singletonList(new XRayClientInterceptor())
        );
        return restTemplate;
    }
}
```

### Metrics with Micrometer and CloudWatch

```java
@Configuration
public class MetricsConfig {
    
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
            .commonTags("application", "user-service")
            .commonTags("environment", System.getenv("ENVIRONMENT"));
    }
    
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}

// Usage in service
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final MeterRegistry meterRegistry;
    private final Counter userCreatedCounter;
    
    public UserService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.userCreatedCounter = Counter.builder("users.created")
            .description("Number of users created")
            .register(meterRegistry);
    }
    
    @Timed(value = "user.creation.time", description = "Time to create user")
    public User createUser(CreateUserRequest request) {
        User user = // ... create user
        userCreatedCounter.increment();
        return user;
    }
}
```

### Centralized Logging with CloudWatch Logs

```yaml
# application.yml
logging:
  pattern:
    console: '{"timestamp":"%d{ISO8601}","level":"%level","service":"${spring.application.name}","traceId":"%X{X-Amzn-Trace-Id}","message":"%msg"}%n'
  level:
    root: INFO
    io.spring: DEBUG
```

### Health Checks and Readiness Probes

```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    private final DataSource dataSource;
    
    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1)) {
                return Health.up()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("status", "Connected")
                    .build();
            }
        } catch (SQLException e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
        return Health.down().build();
    }
}

@Component
public class DependencyHealthIndicator implements HealthIndicator {
    
    private final UserClient userClient;
    
    @Override
    public Health health() {
        try {
            userClient.healthCheck();
            return Health.up()
                .withDetail("user-service", "Available")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("user-service", "Unavailable")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

---

## Testing Strategy

### Unit Testing

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void createUser_WithValidData_ReturnsUser() {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
            .email("test@example.com")
            .username("testuser")
            .password("password123")
            .build();
        
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        
        // When
        User result = userService.createUser(request);
        
        // Then
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(userRepository).save(any(User.class));
    }
}
```

### Integration Testing with Testcontainers

```java
@SpringBootTest
@Testcontainers
class UserRepositoryIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void findByEmail_WithExistingUser_ReturnsUser() {
        // Given
        User user = User.builder()
            .email("test@example.com")
            .username("testuser")
            .passwordHash("hash")
            .build();
        userRepository.save(user);
        
        // When
        Optional<User> result = userRepository.findByEmail("test@example.com");
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
    }
}
```

### Contract Testing with Spring Cloud Contract

```groovy
// contracts/user-service/shouldReturnUserById.groovy
Contract.make {
    description "should return user by id"
    request {
        method GET()
        url "/api/users/123"
        headers {
            contentType applicationJson()
        }
    }
    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body([
            id: "123",
            email: "user@example.com",
            username: "testuser"
        ])
    }
}
```

### End-to-End Testing

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ArticleE2ETest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void createAndRetrieveArticle() {
        // Create user and get token
        String token = createUserAndGetToken();
        
        // Create article
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        
        CreateArticleRequest request = CreateArticleRequest.builder()
            .title("Test Article")
            .description("Test Description")
            .body("Test Body")
            .tagList(List.of("test", "java"))
            .build();
        
        ResponseEntity<ArticleResponse> createResponse = restTemplate.exchange(
            "/api/articles",
            HttpMethod.POST,
            new HttpEntity<>(request, headers),
            ArticleResponse.class
        );
        
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        
        // Retrieve article
        String slug = createResponse.getBody().getArticle().getSlug();
        ResponseEntity<ArticleResponse> getResponse = restTemplate.getForEntity(
            "/api/articles/" + slug,
            ArticleResponse.class
        );
        
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getArticle().getTitle()).isEqualTo("Test Article");
    }
}
```

---

## Deployment Strategy

### Blue-Green Deployment

```yaml
# AWS CodeDeploy appspec.yml
version: 0.0
Resources:
  - TargetService:
      Type: AWS::ECS::Service
      Properties:
        TaskDefinition: "arn:aws:ecs:region:account:task-definition/user-service"
        LoadBalancerInfo:
          ContainerName: "user-service"
          ContainerPort: 8081
        PlatformVersion: "LATEST"

Hooks:
  - BeforeInstall: "scripts/before_install.sh"
  - AfterInstall: "scripts/after_install.sh"
  - AfterAllowTestTraffic: "scripts/run_tests.sh"
  - BeforeAllowTraffic: "scripts/before_traffic.sh"
  - AfterAllowTraffic: "scripts/after_traffic.sh"
```

### Canary Deployment with AWS App Mesh

```yaml
# Virtual router configuration for canary
apiVersion: appmesh.k8s.aws/v1beta2
kind: VirtualRouter
metadata:
  name: user-service-router
  namespace: realworld
spec:
  listeners:
    - portMapping:
        port: 8081
        protocol: http
  routes:
    - name: user-service-route
      httpRoute:
        match:
          prefix: /
        action:
          weightedTargets:
            - virtualNodeRef:
                name: user-service-v1
              weight: 90
            - virtualNodeRef:
                name: user-service-v2
              weight: 10
```

### Rollback Strategy

```bash
#!/bin/bash
# rollback.sh - Emergency rollback script

SERVICE_NAME=$1
PREVIOUS_TASK_DEF=$2

echo "Rolling back $SERVICE_NAME to $PREVIOUS_TASK_DEF"

aws ecs update-service \
  --cluster realworld-production \
  --service $SERVICE_NAME \
  --task-definition $PREVIOUS_TASK_DEF \
  --force-new-deployment

aws ecs wait services-stable \
  --cluster realworld-production \
  --services $SERVICE_NAME

echo "Rollback complete"
```

---

## Migration Checklist

### Pre-Migration

- [ ] Complete architecture assessment and documentation
- [ ] Identify all bounded contexts and service boundaries
- [ ] Map all inter-module dependencies
- [ ] Document all external integrations
- [ ] Establish baseline performance metrics
- [ ] Set up monitoring and alerting infrastructure
- [ ] Create rollback procedures
- [ ] Train team on microservices patterns

### Infrastructure Setup

- [ ] Set up AWS VPC with proper networking
- [ ] Configure ECS/EKS cluster
- [ ] Set up Amazon RDS instances for each service
- [ ] Configure Amazon ECR for container images
- [ ] Set up AWS Secrets Manager for credentials
- [ ] Configure Application Load Balancer
- [ ] Set up CloudWatch for logging and metrics
- [ ] Configure AWS X-Ray for distributed tracing

### Service Migration (Per Service)

- [ ] Extract service code from monolith
- [ ] Create independent Gradle build configuration
- [ ] Set up service-specific database
- [ ] Implement API contracts
- [ ] Add health checks and readiness probes
- [ ] Write unit and integration tests
- [ ] Create Docker image
- [ ] Deploy to staging environment
- [ ] Run performance tests
- [ ] Deploy to production with canary
- [ ] Monitor for issues
- [ ] Remove code from monolith

### Post-Migration

- [ ] Decommission monolith database tables
- [ ] Remove monolith deployment
- [ ] Update documentation
- [ ] Conduct post-mortem review
- [ ] Optimize based on production metrics

---

## Appendix

### Useful Commands

```bash
# Build all services
./gradlew build

# Run tests for specific service
./gradlew :user-service:test

# Build Docker images
./gradlew :user-service:bootBuildImage

# Deploy to ECS
aws ecs update-service --cluster realworld --service user-service --force-new-deployment

# View service logs
aws logs tail /ecs/user-service --follow

# Scale service
aws ecs update-service --cluster realworld --service user-service --desired-count 5
```

### Reference Architecture Diagram

```
                                    ┌─────────────────┐
                                    │   CloudFront    │
                                    │      CDN        │
                                    └────────┬────────┘
                                             │
                                    ┌────────▼────────┐
                                    │   API Gateway   │
                                    │   (Regional)    │
                                    └────────┬────────┘
                                             │
                    ┌────────────────────────┼────────────────────────┐
                    │                        │                        │
           ┌────────▼────────┐     ┌────────▼────────┐     ┌────────▼────────┐
           │  User Service   │     │ Article Service │     │ Social Service  │
           │   (Fargate)     │     │   (Fargate)     │     │   (Fargate)     │
           └────────┬────────┘     └────────┬────────┘     └────────┬────────┘
                    │                        │                        │
           ┌────────▼────────┐     ┌────────▼────────┐     ┌────────▼────────┐
           │   PostgreSQL    │     │   PostgreSQL    │     │   PostgreSQL    │
           │   (RDS)         │     │   (RDS)         │     │   (RDS)         │
           └─────────────────┘     └─────────────────┘     └─────────────────┘
                    │                        │                        │
                    └────────────────────────┼────────────────────────┘
                                             │
                                    ┌────────▼────────┐
                                    │   Amazon SQS    │
                                    │   (Events)      │
                                    └─────────────────┘
```

### Additional Resources

For further reading, consult the Spring Boot documentation at https://docs.spring.io/spring-boot/docs/current/reference/html/, AWS ECS documentation at https://docs.aws.amazon.com/ecs/, AWS EKS documentation at https://docs.aws.amazon.com/eks/, and the Microservices Patterns book by Chris Richardson.

---

*This playbook is a living document and should be updated as the migration progresses and lessons are learned.*
