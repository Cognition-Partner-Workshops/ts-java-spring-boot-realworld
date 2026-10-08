# Modernization baseline (captured from `main` @ c9f94b1)

Captured 2026-10-08 on Ubuntu 22.04, JDK 11 (`/usr/lib/jvm/java-11-openjdk-amd64`), Gradle wrapper 7.4, Node 16.20.2 / npm 8.19.4.

> Note: the actual baseline is **Spring Boot 2.6.3 / Java 11** (not 2.3 as stated in the brief).

## Command results

| Command | Result |
|---|---|
| `./gradlew clean build -x test` | PASS |
| `./gradlew clean test spotlessCheck` | tests PASS (68/68), spotless PASS, **build FAILS** because `test` is `finalizedBy jacocoTestReport -> jacocoTestCoverageVerification` and coverage is below the 80% gate |
| `./gradlew jacocoTestReport jacocoTestCoverageVerification` | **FAIL** – instruction coverage 0.33 < 0.80 |
| `./gradlew dependencies --configuration runtimeClasspath` | see `gradle-runtimeClasspath.txt` |
| `npm install` (frontend) | PASS (Node 16) |
| `npm audit` (frontend) | **109 vulnerabilities** (10 critical, 39 high, 54 moderate, 6 low) – see `npm-audit.md` |

CI (`.github/workflows/gradle.yml`) currently runs `./gradlew clean test -x jacocoTestCoverageVerification`, i.e. it skips the coverage gate.

## Tests

- 68 JUnit 5 tests in 21 classes, 0 failures, 0 skipped (Selenium/TestNG suite excluded from `test`).

## JaCoCo coverage (bundle)

| Counter | Covered | Missed | % |
|---|---|---|---|
| INSTRUCTION | 3486 | 7011 | 33.2 |
| BRANCH | 146 | 744 | 16.4 |
| LINE | 738 | 1429 | 34.1 |
| METHOD | 347 | 532 | 39.5 |
| CLASS | 86 | 73 | 54.1 |

Per package (instructions): `io/spring/graphql/types` (DGS-generated) 0% with 2974 missed instructions; excluding generated `types`/`client` code the bundle is at 46.3%. `io/spring/graphql` 4.7%, `io/spring/graphql/exception` 3.0%, `io/spring/application/data` 25.6%, `io/spring/application/article` 20.3%.

## Direct dependencies (build.gradle)

| Coordinate | Declared | Resolved |
|---|---|---|
| org.springframework.boot (plugin) | 2.6.3 | 2.6.3 |
| io.spring.dependency-management (plugin) | 1.0.11.RELEASE | |
| com.netflix.dgs.codegen (plugin) | 5.0.6 | |
| com.diffplug.spotless (plugin) | 6.2.1 | |
| jacoco toolVersion | 0.8.7 | |
| Java source/target | 11 | |
| Gradle wrapper | 7.4 | |
| spring-boot-starter-web/validation/hateoas/security | managed | 2.6.3 |
| org.mybatis.spring.boot:mybatis-spring-boot-starter | 2.2.2 | 2.2.2 (mybatis 3.5.9, mybatis-spring 2.0.7) |
| com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter | 4.9.21 | 4.9.21 (graphql-java 17.3) |
| org.flywaydb:flyway-core | managed | 8.0.5 |
| io.jsonwebtoken:jjwt-api/impl/jackson | 0.11.2 | 0.11.2 |
| joda-time:joda-time | 2.10.13 | 2.10.13 |
| org.xerial:sqlite-jdbc | 3.36.0.3 | 3.36.0.3 |
| org.projectlombok:lombok | managed | 1.18.22 |
| test: io.rest-assured:* | 4.5.1 | |
| test: org.mockito:mockito-inline | 4.0.0 | |
| test: selenium-java / webdrivermanager / testng / extentreports / httpclient5 | 4.15.0 / 5.6.2 / 7.8.0 / 5.1.1 / 5.2.1 | |

## Key resolved transitive versions (runtimeClasspath)

| Artifact | Version |
|---|---|
| org.springframework:spring-core | 5.3.15 |
| org.springframework.security:spring-security-core | 5.6.1 |
| org.springframework.hateoas:spring-hateoas | 1.4.1 |
| com.fasterxml.jackson.core:jackson-databind | 2.13.1 |
| org.apache.tomcat.embed:tomcat-embed-core | 9.0.56 |
| org.hibernate.validator:hibernate-validator | 6.2.0.Final |
| com.graphql-java:graphql-java | 17.3 |
| org.yaml:snakeyaml | 1.29 |
| ch.qos.logback:logback-classic | 1.2.10 |
| org.apache.logging.log4j:log4j-api | 2.17.1 |

Full trees: `gradle-runtimeClasspath.txt`, `gradle-testRuntimeClasspath.txt`, `npm-ls-all.json`, `npm-ls-direct.txt`.

## Frontend direct dependencies

axios 0.19.2, lazysizes 5.2.2, marked 1.1.1, next 9.5.1, react 16.13.1, react-dom 16.13.1, swr 0.3.0, @types/node 14.0.27, @types/react 16.9.44, typescript 3.9.7. `engines.node: >=14 <=16`. No lint/typecheck/test scripts.
