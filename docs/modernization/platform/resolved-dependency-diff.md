# Resolved dependency diff — baseline vs. platform/build branch

- **Before**: `docs/modernization/baseline/gradle-runtimeClasspath.txt` (main @ c9f94b1, Gradle 7.4, JDK 11, Spring Boot 2.6.3).
- **After**: `docs/modernization/platform/gradle-runtimeClasspath-after.txt`, produced on this branch with
  `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew dependencies --configuration runtimeClasspath`
  (Gradle 8.14.6, JDK 21, Spring Boot 3.5.16). Resolution succeeds with **0 failures / 0 unresolved conflicts**
  (Gradle resolves every version range deterministically; `->` entries below are BOM-managed upgrades, not errors).
  `testRuntimeClasspath` also resolves cleanly (key test versions listed at the end).

## Build tooling

| Item | Before | After |
|---|---|---|
| Gradle wrapper | 7.4 | **8.14.6** (`-bin` distribution) |
| Java | `sourceCompatibility`/`targetCompatibility` 11 | **Java toolchain 21** (`java.toolchain.languageVersion = 21`); `.java-version` = `21` |
| `org.springframework.boot` plugin | 2.6.3 | **3.5.16** (latest 3.5.x on Maven Central at time of writing) |
| `io.spring.dependency-management` | 1.0.11.RELEASE | **1.1.7** |
| `com.netflix.dgs.codegen` plugin | 5.0.6 | **8.7.0** (latest 8.x) |
| `com.diffplug.spotless` | 6.2.1 | **7.0.4** (latest 7.0.x; `googleJavaFormat()` unchanged) |
| JaCoCo `toolVersion` | 0.8.7 | **0.8.13** |
| BOM imports | (Boot only) | Boot 3.5.16 + **`com.netflix.graphql.dgs:graphql-dgs-platform-dependencies:10.6.0`** |

## Direct dependencies

| Coordinate | Before (declared → resolved) | After (declared → resolved) |
|---|---|---|
| `org.springframework.boot:spring-boot-starter-web` | managed → 2.6.3 | managed → **3.5.16** |
| `org.springframework.boot:spring-boot-starter-validation` | managed → 2.6.3 | managed → **3.5.16** |
| `org.springframework.boot:spring-boot-starter-hateoas` | managed → 2.6.3 | managed → **3.5.16** |
| `org.springframework.boot:spring-boot-starter-security` | managed → 2.6.3 | managed → **3.5.16** |
| `org.mybatis.spring.boot:mybatis-spring-boot-starter` | 2.2.2 | **3.0.4** |
| `com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter` | 4.9.21 | **removed** (artifact no longer exists in DGS 10) |
| `com.netflix.graphql.dgs:graphql-dgs-spring-graphql-starter` | — | **managed by DGS platform BOM → 10.6.0** |
| `org.flywaydb:flyway-core` | managed → 8.0.5 | managed → **11.7.2** |
| `io.jsonwebtoken:jjwt-api` / `jjwt-impl` / `jjwt-jackson` | 0.11.2 | **0.12.6** |
| `joda-time:joda-time` | 2.10.13 | **removed** (java.time only) |
| `org.xerial:sqlite-jdbc` | 3.36.0.3 | **3.50.3.0** |
| `org.projectlombok:lombok` (compileOnly + annotationProcessor) | managed → 1.18.22 | managed → **1.18.46** |
| `com.netflix.graphql.dgs.codegen:graphql-dgs-codegen-client-core` (added by codegen plugin) | 5.0.6 | replaced by **`graphql-dgs-codegen-shared-core:8.7.0`** (added automatically by codegen 8.x) |

### Test-only direct dependencies

| Coordinate | Before | After |
|---|---|---|
| `io.rest-assured:rest-assured` / `json-path` / `xml-path` / `spring-mock-mvc` | 4.5.1 | **5.5.7** |
| `org.springframework.security:spring-security-test` | managed → 5.6.1 | managed → **6.5.11** |
| `org.springframework.boot:spring-boot-starter-test` | managed → 2.6.3 | managed → **3.5.16** |
| `org.mybatis.spring.boot:mybatis-spring-boot-starter-test` | 2.2.2 | **3.0.4** |
| `org.mockito:mockito-inline` | 4.0.0 | **removed** (Boot-managed `mockito-core` 5.17.0; inline mock maker is the default in Mockito 5) |
| `org.seleniumhq.selenium:selenium-java` | 4.15.0 | **4.33.0** |
| `io.github.bonigarcia:webdrivermanager` | 5.6.2 | **6.4.0** |
| `org.testng:testng` | 7.8.0 | **7.11.0** |
| `com.aventstack:extentreports` | 5.1.1 | **5.1.2** (not in the manifest; patch bump only) |
| `org.apache.httpcomponents.client5:httpclient5` | 5.2.1 (explicit) | **managed by Boot BOM → 5.5.2** (explicit version dropped so it cannot drift from the Boot-managed HttpClient 5 line) |

## Key transitive versions (runtimeClasspath)

| Artifact | Before | After |
|---|---|---|
| `org.springframework:spring-core` | 5.3.15 | **6.2.19** |
| `org.springframework.security:spring-security-core` | 5.6.1 | **6.5.11** |
| `org.springframework.hateoas:spring-hateoas` | 1.4.1 | **2.5.3** |
| `org.springframework.graphql:spring-graphql` | — | **1.4.6** (new, pulled in by `graphql-dgs-spring-graphql-starter`) |
| `com.fasterxml.jackson.core:jackson-databind` | 2.13.1 | **2.21.4** (Boot-managed; DGS requests 2.18.3 and jjwt-jackson 2.12.7.1, both upgraded) |
| `org.apache.tomcat.embed:tomcat-embed-core` | 9.0.56 | **10.1.55** |
| `org.hibernate.validator:hibernate-validator` | 6.2.0.Final (javax.validation 2.0.2) | **8.0.3.Final** (`jakarta.validation-api` 3.0.2) |
| `com.graphql-java:graphql-java` | 17.3 | **24.0** (Boot BOM-managed; DGS 10.6.0 requests 24.3 / spring-graphql requests 22.3 — both aligned to 24.0 by the Boot BOM) |
| `com.netflix.graphql.dgs:graphql-dgs` | 4.9.21 | **10.6.0** |
| `org.yaml:snakeyaml` | 1.29 | **2.4** |
| `ch.qos.logback:logback-classic` | 1.2.10 | **1.5.34** |
| `org.apache.logging.log4j:log4j-api` | 2.17.1 | **2.25.2** |
| `org.slf4j:slf4j-api` | 1.7.33 | **2.0.18** |
| `org.flywaydb:flyway-core` | 8.0.5 | **11.7.2** |
| `org.mybatis:mybatis` | 3.5.9 | **3.5.17** |
| `org.mybatis:mybatis-spring` | 2.0.7 | **3.0.4** |
| `org.xerial:sqlite-jdbc` | 3.36.0.3 | **3.50.3.0** |
| `org.projectlombok:lombok` | 1.18.22 | **1.18.46** |
| `io.micrometer:micrometer-observation` | — | **1.15.12** (new in Spring 6) |

### Key test transitives (testRuntimeClasspath)

| Artifact | Before | After |
|---|---|---|
| `org.junit.jupiter:junit-jupiter` | 5.8.2 | **5.12.2** |
| `org.mockito:mockito-core` | 4.0.0 | **5.17.0** |
| `net.bytebuddy:byte-buddy` | 1.11.22 | **1.17.8** |
| `org.apache.groovy:groovy` (REST-Assured) | 3.0.9 (`org.codehaus.groovy`) | **4.0.32** (`org.apache.groovy`) |

## Notes

- The Boot 3.5 BOM wins every version alignment (Jackson, graphql-java, httpclient5, slf4j); this is the intended behaviour of `io.spring.dependency-management` and keeps the DGS/Spring GraphQL stack on the versions Boot 3.5.16 was tested with.
- `graphql-java` is aligned *down* from the 24.3 that DGS 10.6.0 declares to the Boot-managed 24.0. This is a BOM alignment within the same major/minor line, not a manifest downgrade; if the GraphQL session needs 24.3 specifically it can be pinned via `ext['graphql-java.version']`.
- No `javax.*` artifacts remain on the runtime classpath (validation is `jakarta.validation-api:3.0.2`, servlet API comes from Tomcat 10.1 `tomcat-embed-core`), so the "dual validation provider" pitfall cannot occur once sources are migrated to `jakarta.*`.
