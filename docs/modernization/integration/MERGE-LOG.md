# Integration merge log — `devin/1791491209-modernization-integration`

Gate command run after every merge (JDK 21):

```
./gradlew clean test spotlessCheck jacocoTestReport jacocoTestCoverageVerification
```

| # | Branch merged | PR | Merge commit | Gate result | Tests | Instr. coverage |
|---|---------------|----|--------------|-------------|-------|-----------------|
| 0 | baseline (main `c9f94b1` + baseline docs) | — | `b4ea605` | FAIL at `jacocoTestCoverageVerification` (33.2% < 80%) | 68 / 0 failed | 33.2% |
| 1 | `devin/1791491209-modernization-platform-build` | #226 | `79ac2d5` | FAIL at `compileJava` (expected: `src/**` still Boot 2 / javax / Joda; 200 compiler errors) and `spotlessJava` (Gradle 8 implicit-dependency validation — Spotless target `fileTree(rootDir)` includes `build/` outputs; fixed by REST branch's `target 'src/**/*.java'`) | not run | n/a |
| 2 | `devin/1791491209-modernization-rest-security` | #227 | `e9f0b1c` | compile/tests/Spotless PASS; FAIL only at `jacocoTestCoverageVerification` (52.6% < 80%, regression suite pending) | 98 / 0 failed | 52.6% |
| 3 | `devin/1791491209-modernization-persistence-time` | #228 | `81e0c53` | compile/tests/Spotless PASS; FAIL only at `jacocoTestCoverageVerification` (55.7% < 80%). Conflicts resolved centrally: temporary `joda-time` + Joda `DateTimeSerializer` dropped, `DefaultJwtService` from persistence, `WebSecurityConfig`/`JacksonCustomizations`/`BasePage` from REST, Spotless target + DGS `PageInfo` typeMapping kept (identical in both) | 144 / 0 failed | 55.7% |
| 3b | persistence follow-up `fa02b24` (persist `updatedAt`, canonical UTC text timestamps) | #228 | (ff into `14a4f39`'s parent) | — (covered by gate 4) | — | — |
| 4 | `devin/1791491209-modernization-graphql-dgs` | #229 | `14a4f39` | **PASS** — full gate green for the first time (compile, 203 tests, Spotless, JaCoCo 93.2% ≥ 80%). Conflicts resolved centrally: `graphql/**` from GraphQL branch (uses generated `types.PageInfo`, so the `PageInfo` codegen typeMapping was removed), `WebSecurityConfig`/`DefaultJwtService`/`BasePage`/`build.gradle` kept from integration | 203 / 0 failed | 93.2% (branch 79.5%) |
| 5 | `devin/1791491209-modernization-frontend` | #230 | `01aa18a` | **PASS** backend gate unchanged (203 tests, 93.2%); frontend on Node 22: `npm ci` (430 pkgs), `npm run lint` PASS, `npm run typecheck` PASS, `npm test` 110/110 PASS (94.23% stmts / 89.38% branches / 88.88% funcs / 95.52% lines), `npm run build` PASS (Next 15.5.27). Follow-up: untracked `frontend/tsconfig.tsbuildinfo` build artifact and gitignored it | 203 backend + 110 frontend / 0 failed | 93.2% backend |
