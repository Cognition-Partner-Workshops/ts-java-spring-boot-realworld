# Compile report — platform/build branch (Java 21 / Spring Boot 3.5.16)

Command (run on this branch, sources unchanged from the integration branch):

```
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew clean compileJava compileTestJava --continue
```

Result: `generateJava` **SUCCESS** (DGS codegen 8.7.0, 29 generated sources under `build/generated/sources/dgs-codegen`),
`compileJava` **FAILED with 132 errors** (javac was run with `-Xmaxerrs 10000` via a throw-away init script so the
list below is complete), `compileTestJava` **did not run** (it depends on `compileJava`, so `--continue` cannot reach it).
This is expected: the Java sources are owned by the REST/security, persistence/auth/time and GraphQL sessions and
have not been migrated yet. The sections below are the work plan those sessions are executing.

> javac reports import/`cannot find symbol` errors first and stops attributing method bodies in the affected classes,
> so API-level breakages (JJWT 0.12, DGS 10, MyBatis 3, Spring Security 6 DSL) do not show up as compiler errors yet.
> Those categories were identified by static inspection of the sources and are listed as "latent" below; they will
> surface as soon as the import-level errors are fixed.

## Summary

| Category | Compiler errors | Files | Owner (session) |
|---|---|---|---|
| 1. `javax.validation` → `jakarta.validation` | 67 | 19 | REST/security + persistence/auth |
| 2. `javax.servlet` → `jakarta.servlet` | 8 | 1 | REST/security |
| 3. `WebSecurityConfigurerAdapter` removal (Spring Security 6) | 2 (+ latent DSL changes) | 1 | REST/security |
| 4. Joda-Time → `java.time` | 55 | 10 main + 4 test | persistence/auth/time (+ GraphQL/REST for their callers) |
| 5. DGS 10 API | 0 (latent) | 10 | GraphQL |
| 6. JJWT 0.12 API | 0 (latent) | 1 | persistence/auth/time |
| 7. MyBatis 3.0.x / mybatis-spring 3 | 0 (latent) | 2 | persistence/auth/time |
| 8. Other (Mockito 5, REST-Assured 5, Spring 6 misc.) | 0 (latent) | test sources | REST/security + regression suite |
| **Total compiler errors** | **132** | **30 main files** | |

## 1. `javax.validation` → `jakarta.validation` (67 errors, 19 files)

Symbols: `Valid` (10), `NotBlank` (9), `Email` (3), `Constraint` (4), `Payload` (3), `ConstraintValidator` (4),
`ConstraintValidatorContext` (4), `ConstraintViolationException` (3), plus 27 `package javax.validation does not exist`
and 7 `package javax.validation.constraints does not exist` import errors.

- `src/main/java/io/spring/api/ArticleApi.java` (`@Valid`)
- `src/main/java/io/spring/api/ArticlesApi.java` (`@Valid`)
- `src/main/java/io/spring/api/CommentsApi.java` (`@Valid`, `@NotBlank`)
- `src/main/java/io/spring/api/CurrentUserApi.java` (`@Valid`)
- `src/main/java/io/spring/api/UsersApi.java` (`@Valid`, `@NotBlank`, `@Email`)
- `src/main/java/io/spring/api/exception/CustomizeExceptionHandler.java` (`ConstraintViolation`, `ConstraintViolationException`)
- `src/main/java/io/spring/application/article/ArticleCommandService.java` (`@Valid`)
- `src/main/java/io/spring/application/article/DuplicatedArticleConstraint.java` (`Constraint`, `Payload`)
- `src/main/java/io/spring/application/article/DuplicatedArticleValidator.java` (`ConstraintValidator`, `ConstraintValidatorContext`)
- `src/main/java/io/spring/application/article/NewArticleParam.java` (`@NotBlank`)
- `src/main/java/io/spring/application/user/DuplicatedEmailConstraint.java` (`Constraint`, `Payload`)
- `src/main/java/io/spring/application/user/DuplicatedEmailValidator.java` (`ConstraintValidator`, `ConstraintValidatorContext`)
- `src/main/java/io/spring/application/user/DuplicatedUsernameConstraint.java` (`Constraint`, `Payload`)
- `src/main/java/io/spring/application/user/DuplicatedUsernameValidator.java` (`ConstraintValidator`, `ConstraintValidatorContext`)
- `src/main/java/io/spring/application/user/RegisterParam.java` (`@NotBlank`, `@Email`)
- `src/main/java/io/spring/application/user/UpdateUserParam.java` (`@Email`)
- `src/main/java/io/spring/application/user/UserService.java` (`@Valid`, `Constraint`, `ConstraintValidator`, `ConstraintValidatorContext`, `Payload`)
- `src/main/java/io/spring/graphql/exception/GraphQLCustomizeExceptionHandler.java` (`ConstraintViolation`, `ConstraintViolationException`)

Fix: mechanical rename of the `javax.validation.*` imports to `jakarta.validation.*`. The classpath already carries
`jakarta.validation-api:3.0.2` + `hibernate-validator:8.0.3.Final` and **no** `javax.validation` jar, so there is no
dual-provider risk. Test sources contain no `javax.*` imports.

## 2. `javax.servlet` → `jakarta.servlet` (8 errors, 1 file)

- `src/main/java/io/spring/api/security/JwtTokenFilter.java` (`FilterChain`, `ServletException`, `HttpServletRequest`, `HttpServletResponse`)

Fix: `javax.servlet.*` → `jakarta.servlet.*` (Tomcat 10.1 / Servlet 6.0).

## 3. `WebSecurityConfigurerAdapter` (2 errors + latent, 1 file)

- `src/main/java/io/spring/api/security/WebSecurityConfig.java`

Compiler errors: `WebSecurityConfigurerAdapter` no longer exists. Latent (will appear after the import fix):
`http.csrf()/.cors()` non-lambda DSL is removed in Spring Security 6.1+/7, `authorizeRequests()` → `authorizeHttpRequests()`,
`antMatchers(...)` → `requestMatchers(...)`, `configure(HttpSecurity)` → `@Bean SecurityFilterChain`.
`HttpStatusEntryPoint`, `SessionCreationPolicy`, CORS config and `addFilterBefore(jwtTokenFilter(), UsernamePasswordAuthenticationFilter.class)` carry over.
Tests use `@WebMvcTest` + `@Import({WebSecurityConfig.class, JacksonCustomizations.class})`; `@MockBean` is deprecated in Boot 3.4+ (see §8).

## 4. Joda-Time → `java.time` (55 errors, 10 main files; 4 test files by inspection)

Symbols: `org.joda.time.DateTime` (34 `cannot find symbol` + 11 package import errors), `org.joda.time.format.*`
(3 package import errors: `ISODateTimeFormat`/`DateTimeFormat`).

Main:
- `src/main/java/io/spring/JacksonCustomizations.java` (custom Jackson `DateTime` serializer → `Instant`/`OffsetDateTime` serializer or `JavaTimeModule`)
- `src/main/java/io/spring/application/ArticleQueryService.java`
- `src/main/java/io/spring/application/CommentQueryService.java`
- `src/main/java/io/spring/application/DateTimeCursor.java` (cursor pagination encodes `DateTime` millis)
- `src/main/java/io/spring/application/data/ArticleData.java`
- `src/main/java/io/spring/application/data/CommentData.java`
- `src/main/java/io/spring/core/article/Article.java`
- `src/main/java/io/spring/core/comment/Comment.java`
- `src/main/java/io/spring/infrastructure/mybatis/DateTimeHandler.java` (MyBatis `TypeHandler<DateTime>` → `TypeHandler<Instant>` / rely on built-in `InstantTypeHandler`; registered via `mybatis.type-handlers-package`)
- `src/main/java/io/spring/infrastructure/mybatis/readservice/CommentReadService.java`

Test (will fail in `compileTestJava`):
- `src/test/java/io/spring/TestHelper.java`
- `src/test/java/io/spring/api/ArticleApiTest.java` (`new DateTime()`, `ISODateTimeFormat.dateTime().withZoneUTC().print(time)` — asserts the ISO-8601 UTC `createdAt` format; the replacement must keep the same wire format)
- `src/test/java/io/spring/api/ArticlesApiTest.java`
- `src/test/java/io/spring/application/article/ArticleQueryServiceTest.java` (`new DateTime().minusHours(1)`, `CursorPageParameter<DateTime>`)

Fix: `joda-time` has been removed from `build.gradle`; replace `DateTime` with `java.time.Instant`
(or `OffsetDateTime`), keep ISO-8601 UTC output (`DateTimeFormatter.ISO_INSTANT` / Jackson `WRITE_DATES_AS_TIMESTAMPS=false`).

## 5. DGS 10 API (latent, 10 files)

No compiler errors yet (classes blocked by §1/§4 imports), but DGS 4.9 → 10.6 changes to verify:
- `com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter` is replaced by `graphql-dgs-spring-graphql-starter`
  (DGS now runs on Spring for GraphQL). Endpoint stays `/graphql`; GraphiQL is now at `/graphiql` via
  `spring.graphql.graphiql.enabled=true` (default `true` in DGS starter) — `WebSecurityConfig` permits both paths already.
- `DgsComponent`, `DgsQuery`, `DgsMutation`, `DgsData`, `InputArgument`, `DgsDataFetchingEnvironment` — unchanged package
  (`com.netflix.graphql.dgs`), still valid.
- `graphql.execution.DataFetcherExceptionHandler#onException` is **deprecated/removed** in graphql-java 24 in favour of
  `handleException(...)` returning `CompletableFuture<DataFetcherExceptionHandlerResult>`; `DefaultDataFetcherExceptionHandler`
  also moved to the async API. `com.netflix.graphql.types.errors.TypedGraphQLError` / `ErrorType` still exist.
- `graphql.relay.DefaultPageInfo`, `DefaultConnectionCursor`, `DataFetcherResult` — unchanged.
- Generated code: `io.spring.graphql.types.*`, `io.spring.graphql.client.*`, `io.spring.graphql.DgsConstants` regenerate
  with codegen 8.7.0 (29 files; generated `types` classes now ship `Builder`s and `equals/hashCode`, package layout unchanged).

Files: `ArticleDatafetcher`, `ArticleMutation`, `CommentDatafetcher`, `CommentMutation`, `MeDatafetcher`, `ProfileDatafetcher`,
`RelationMutation`, `TagDatafetcher`, `UserMutation`, `exception/GraphQLCustomizeExceptionHandler` (all under `src/main/java/io/spring/graphql/`).

## 6. JJWT 0.12 API (latent, 1 file)

- `src/main/java/io/spring/infrastructure/service/DefaultJwtService.java`
  - `Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)` → `Jwts.parser().verifyWith(key).build().parseSignedClaims(token)`
  - `io.jsonwebtoken.SignatureAlgorithm` + `new SecretKeySpec(...)` → `Jwts.SIG.HS512.key()` / `Keys.hmacShaKeyFor(bytes)`; `signWith(key)` infers the algorithm
  - `Jws<Claims>#getBody()` → `getPayload()`
  - `setSubject/setExpiration` builder methods → `subject(...)/expiration(...)` (old names deprecated, still compile)

## 7. MyBatis 3.0.x / mybatis-spring 3 (latent, 2 files)

- `src/main/java/io/spring/infrastructure/mybatis/DateTimeHandler.java` — rewrite for `java.time` (see §4) or delete and rely on MyBatis' built-in `InstantTypeHandler`.
- `src/test/java/io/spring/infrastructure/DbTestBase.java` — `@MybatisTest` (`org.mybatis.spring.boot.test.autoconfigure.MybatisTest`) and
  `@AutoConfigureTestDatabase(replace = NONE)` are unchanged in `mybatis-spring-boot-starter-test:3.0.4`.
- Mapper interfaces (`io.spring.infrastructure.mybatis.mapper.*`) and XML mappers (`src/main/resources/mapper/*.xml`) need no changes; `mybatis.*` properties are unchanged.
- Flyway 11 with SQLite: migrations `V1__create_tables.sql`/`V2__seed_data.sql` are plain SQL and unaffected; Flyway 11 removed the
  `spring.flyway.*` legacy keys only for Oracle/SQL Server-specific features.

## 8. Other (latent, test sources)

- `src/test/java/io/spring/api/*ApiTest.java`, `TestWithCurrentUser.java` use `@MockBean`
  (`org.springframework.boot.test.mock.mockito.MockBean`) — deprecated since Boot 3.4 in favour of
  `@MockitoBean`; still compiles on 3.5.x with deprecation warnings (Spotless does not fail on warnings).
- `mockito-inline` removed: Mockito 5.17 uses the inline mock maker by default; no source change expected.
- REST-Assured 5.5.7 `spring-mock-mvc`: same `RestAssuredMockMvc` API; Groovy moved to `org.apache.groovy` 4 (no source impact).
- `spring-boot-starter-hateoas` 3.5: `io.spring.api.*` only use Spring MVC/`ResponseEntity`; HATEOAS 2.x package names unchanged.
- Spring 6 removed `org.springframework.util` trailing-slash matching for MVC (`/articles/` ≠ `/articles`); REST tests should confirm paths.

## Spotless

```
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew spotlessCheck
```

**BUILD SUCCESSFUL** — Spotless 7.0.4 with the plugin-default google-java-format reports **no violations** on the
current (unmigrated) sources, so no reformatting is required and no sources outside this session's ownership were touched.

## Baseline verification on this branch

| Check | Result |
|---|---|
| `./gradlew --version` | Gradle 8.14.6, Launcher/Daemon JVM 21.0.12.1 (`/usr/lib/jvm/java-21-openjdk-amd64`) |
| `./gradlew dependencies --configuration runtimeClasspath` | SUCCESS, 0 failures — `docs/modernization/platform/gradle-runtimeClasspath-after.txt` |
| `./gradlew dependencies --configuration testRuntimeClasspath` | SUCCESS, 0 failures |
| `./gradlew clean generateJava` | SUCCESS (DGS codegen 8.7.0, 29 sources) |
| `./gradlew clean compileJava compileTestJava --continue` | FAILED, 132 errors (this report) |
| `./gradlew spotlessCheck` | SUCCESS |
| `./gradlew clean test ...` | not runnable until the source sessions land |
