# Modernization report — Java 21 / Spring Boot 3.5 / Next 15

Integration branch: `devin/1791491209-modernization-integration` (never merged to `main`).
Baseline: `b4ea605` (Spring Boot **2.6.3** / Gradle 7.4 / Java 11 — the README said 2.3; the real baseline was recorded in
[`baseline/BASELINE.md`](baseline/BASELINE.md) rather than changed).

## 1. Child branches / PRs (all targeting the integration branch)

| # | Scope | Branch | PR | Merged into integration as |
|---|-------|--------|----|----------------------------|
| 1 | platform / build / CI | `devin/1791491209-modernization-platform-build` | #226 | `79ac2d5` |
| 2 | REST / security / Jakarta | `devin/1791491209-modernization-rest-security` | #227 | `e9f0b1c` |
| 3 | persistence / JWT / java.time | `devin/1791491209-modernization-persistence-time` | #228 | `81e0c53` (+ follow-up `fa02b24`) |
| 4 | GraphQL / DGS 10 | `devin/1791491209-modernization-graphql-dgs` | #229 | `14a4f39` |
| 5 | frontend (Next 15 / React 19 / Vitest) | `devin/1791491209-modernization-frontend` | #230 | `01aa18a` |
| 6 | cross-cutting regression suite | `devin/1791491209-modernization-regression-suite` | #231 | _see §6_ |

Per-merge gate results: [`integration/MERGE-LOG.md`](integration/MERGE-LOG.md).

## 2. Direct dependency versions — before / after

| Artifact | Before | After |
|----------|--------|-------|
| Java | 11 | **21** (Gradle toolchain, `.java-version`) |
| Gradle wrapper | 7.4 | **8.14.6** |
| `org.springframework.boot` | 2.6.3 | **3.5.16** |
| `io.spring.dependency-management` | 1.0.11.RELEASE | **1.1.7** |
| `com.diffplug.spotless` | 6.2.1 | **7.0.4** (target `src/**/*.java`) |
| JaCoCo | 0.8.7 | **0.8.13**, threshold **0.80 unchanged** |
| `com.netflix.dgs.codegen` | 5.0.6 | **8.7.0** |
| DGS runtime | `graphql-dgs-spring-boot-starter` 4.9.21 | `graphql-dgs-platform-dependencies` BOM **10.6.0** + `graphql-dgs-spring-graphql-starter` |
| `org.mybatis.spring.boot:mybatis-spring-boot-starter` | 2.2.2 | **3.0.4** |
| `org.xerial:sqlite-jdbc` | 3.36.0.3 | **3.50.3.0** |
| `io.jsonwebtoken:jjwt-api/impl/jackson` | 0.11.2 | **0.12.6** |
| `joda-time:joda-time` | 2.10.13 | **removed** (→ `java.time.Instant`) |
| `org.flywaydb:flyway-core` | 8.0.5 (Boot-managed) | **11.7.2** (Boot-managed) |
| `io.rest-assured:spring-mock-mvc` (test) | 4.5.1 | **5.5.7** |
| Selenium / WebDriverManager / TestNG / ExtentReports (test) | 4.15.0 / 5.x / 7.x / 5.x | **4.33.0 / 6.4.0 / 7.11.0 / 5.1.2** |
| Lombok (annotationProcessor) | 1.18.26 | **1.18.46** (Boot-managed) |

Frontend (`frontend/package.json`):

| Package | Before | After |
|---------|--------|-------|
| Node engine | 16 | **>=22** (`.nvmrc` = 22) |
| `next` | 9.5.1 | **15.5.27** |
| `react` / `react-dom` | 16.13.1 | **19.3.0** |
| `typescript` | 3.9.7 | **5.9.3** |
| `axios` | 0.19.2 | **1.20.0** |
| `swr` | 0.3.0 | **2.5.1** |
| `marked` | 1.1.1 | **18.1.0** + `isomorphic-dompurify` **4.5.0** (sanitized before `dangerouslySetInnerHTML`) |
| `lazysizes` | 5.2.2 | **5.3.2** |
| `@types/node` / `@types/react` / `@types/react-dom` | 14 / 16.9 / — | **22.20 / 19.3 / 19.3** |
| tooling | none | `eslint` 9.39 (flat config) + `eslint-config-next` 15.5, `vitest` 5.0.3 + `@vitest/coverage-v8`, `jsdom` 30, Testing Library (react 16.3 / jest-dom 6.9 / user-event 14.6) |

## 3. Resolved transitive versions (runtimeClasspath) — before / after

Full trees: [`baseline/gradle-runtimeClasspath.txt`](baseline/gradle-runtimeClasspath.txt) (112 artifacts) →
[`integration/gradle-runtimeClasspath-after.txt`](integration/gradle-runtimeClasspath-after.txt) (96 artifacts);
test classpath: [`baseline/gradle-testRuntimeClasspath.txt`](baseline/gradle-testRuntimeClasspath.txt) →
[`integration/gradle-testRuntimeClasspath-after.txt`](integration/gradle-testRuntimeClasspath-after.txt).
Platform-session diff with rationale: [`platform/resolved-dependency-diff.md`](platform/resolved-dependency-diff.md).

| Artifact | Before | After |
|----------|--------|-------|
| `org.springframework:spring-core` | 5.3.15 | 6.2.19 |
| `org.springframework.security:spring-security-core` | 5.6.1 | 6.5.11 |
| `org.apache.tomcat.embed:tomcat-embed-core` | 9.0.56 | 10.1.55 |
| `com.fasterxml.jackson.core:jackson-databind` | 2.13.1 | 2.21.4 |
| `com.graphql-java:graphql-java` | 17.3 | 24.0 |
| `com.netflix.graphql.dgs:graphql-dgs` | 4.9.21 | 10.6.0 |
| `org.mybatis:mybatis` | 3.5.9 | 3.5.17 |
| `org.hibernate.validator:hibernate-validator` | 6.2.0.Final | 8.0.3.Final |
| `jakarta.validation:jakarta.validation-api` | 2.0.2 (javax namespace) | 3.0.2 (jakarta namespace) |
| `ch.qos.logback:logback-classic` | 1.2.10 | 1.5.34 |
| `org.flywaydb:flyway-core` | 8.0.5 | 11.7.2 |
| `joda-time:joda-time` | 2.10.13 | — |

## 4. Tests and coverage

| Stage | Backend tests | JaCoCo instruction | Frontend tests | Frontend coverage |
|-------|---------------|--------------------|----------------|-------------------|
| Baseline `b4ea605` (Java 11 / Boot 2.6.3) | 68 / 0 failed | **33.2%** — gate FAILS (46.3% excl. generated DGS types); CI skipped the gate with `-x jacocoTestCoverageVerification` | none | none |
| After platform merge | n/a — `compileJava` fails until REST/persistence land (expected) | — | — | — |
| After REST merge | 98 / 0 | 52.6% (gate fails) | — | — |
| After persistence merge | 144 / 0 | 55.7% (gate fails) | — | — |
| After GraphQL merge | 203 / 0 | **93.2%** (branch 79.5%) — **gate PASSES** | — | — |
| After frontend merge | 203 / 0 | 93.2% | 110 / 0 | 94.23% stmts / 89.38% branches / 88.88% funcs / 95.52% lines (thresholds 85/80/80/85) |
| After regression suite (Phase 2) | _TBD_ | _TBD_ | _TBD_ | _TBD_ |

JaCoCo policy: threshold **0.80 unchanged**; the only exclusions are DGS codegen output
(`io/spring/graphql/types/**`, `io/spring/graphql/client/**`, `io/spring/graphql/DgsConstants*`). No production class or test was excluded or disabled.

## 5. npm audit — before / after

| | critical | high | moderate | low | total |
|---|---|---|---|---|---|
| Baseline (Next 9.5 / Node 16) — [`baseline/npm-audit.md`](baseline/npm-audit.md) | 10 | 39 | 54 | 6 | **109** |
| After (Next 15.5 / Node 22) — [`integration/npm-audit-after.json`](integration/npm-audit-after.json) | 0 | 6 | 1 | 0 | **7** |

Remaining 7 are dev/build-time only (`braces`/`micromatch`/`fast-glob` via `eslint-config-next@15.5.27`; `postcss <= 8.5.22` pinned by `next@15.5.27`).
npm's only fixes are a downgrade to `eslint-config-next@14` or `next@16` (outside the Next 15 target) — deferred and documented in [`../../frontend/AUDIT.md`](../../frontend/AUDIT.md).

## 6. Known API migration notes

**Build / platform** — Java 21 toolchain; Gradle 8.14.6 (Spotless must target `src/**/*.java`, a root `fileTree` including `build/` trips Gradle 8 implicit-dependency validation); Boot 3.5.16 BOM manages Flyway 11, Jackson 2.21, Tomcat 10.1; HttpClient 5 for REST-Assured; CI (`.github/workflows/gradle.yml`) runs JDK 21 with the *full* gate (coverage enforced) plus a Node 22 frontend job (`npm ci`, lint, typecheck, test, build).

**REST / security** — `javax.*` → `jakarta.*`; `WebSecurityConfigurerAdapter` → `@Bean SecurityFilterChain` with lambda DSL, `antMatchers` → `requestMatchers`, `authorizeRequests` → `authorizeHttpRequests`; `Authorization: Token <jwt>` header convention preserved. **Runtime finding:** Spring Security 6 also filters the servlet ERROR dispatch, which turned `@ResponseStatus` 403/404 responses into 401 at runtime (not visible under MockMvc) — fixed with `dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()`.

**Persistence / JWT / time** — Joda `DateTime` → `java.time.Instant` (UTC, millisecond precision) across domain, DTOs, cursor pagination (wire format unchanged: epoch-millis string), MyBatis `DateTimeHandler` → `InstantHandler` (writes canonical UTC text `yyyy-MM-dd HH:mm:ss.SSS` so app rows and seeded rows order/compare consistently; reads text with/without fraction, ISO offsets and legacy epoch-millis), Jackson `InstantSerializer` emits `uuuu-MM-dd'T'HH:mm:ss.SSSX` (identical to the old Joda output). `ArticleMapper.update` now persists `updated_at` (pre-existing defect found by the regression suite). JJWT 0.11 → 0.12.6: `Keys.hmacShaKeyFor`, `Jwts.builder().subject()/expiration()/signWith(key)`, `Jwts.parser().verifyWith(key).build().parseSignedClaims()`; expired / tampered / wrong-key / malformed / `alg=none` tokens → `Optional.empty()` (401). Flyway 11 (Boot-managed) still supports SQLite in `flyway-core`.

**GraphQL / DGS** — DGS 4.9 → 10.6 on `graphql-dgs-spring-graphql-starter` (Spring GraphQL under the hood), codegen 5 → 8.7 (generated `types.PageInfo` used directly; no `PageInfo` type mapping); `DataFetcherExceptionHandler.onException` → async `handleException` returning `CompletableFuture`; `/graphql` and GraphiQL (`/graphiql` → `/graphiql?path=/graphql`) preserved; `DgsQueryExecutor`-based tests replaced `@MockBean` with `@MockitoBean`.

**Frontend** — Next 9 → 15: `next/link` no longer wraps `<a>` (CustomLink/NavLink/ArticlePreview/Navbar rewritten), `_document` on `Html/Head/Main/NextScript`, `next.config.js` added; React 19 / TS 5.9: explicit `children: React.ReactNode`, typed handlers/reducers; SWR 2: `trigger()` → `mutate()`, `initialData` → `fallbackData`, `isLoading` guards avoid redirect flash; axios 1: `AxiosError` narrowing, token read from parsed `localStorage` user (old code sent `Token undefined`); `marked` 18 output sanitized with DOMPurify (tests prove `<script>`, `onerror`, `javascript:` and `<iframe>` payloads are stripped); wrong-password login now surfaces the backend `{"message": ...}` error; stale article preview after edit fixed.

## 7. Deferred items / remaining vulnerabilities

- npm: 7 dev-time findings (see §5) — resolvable only by Next 16 / eslint-config-next 14.
- `frontend` images stay `<img>` + lazysizes (arbitrary remote avatar hosts) rather than `next/image`.
- _Regression Phase 2 defects: TBD_

## 8. Verification commands (integration branch head)

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew clean test spotlessCheck jacocoTestReport jacocoTestCoverageVerification   # BUILD SUCCESSFUL
source ~/.nvm/nvm.sh && nvm use 22 && cd frontend && npm ci && npm run lint && npm run typecheck && npm test && npm run build       # all green
grep -r joda src/ build.gradle                                                                                                     # no production references
```
