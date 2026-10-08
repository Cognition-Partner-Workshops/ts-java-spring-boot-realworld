# Integration merge log — `devin/1791491209-modernization-integration`

Gate command run after every merge (JDK 21):

```
./gradlew clean test spotlessCheck jacocoTestReport jacocoTestCoverageVerification
```

| # | Branch merged | PR | Merge commit | Gate result | Tests | Instr. coverage |
|---|---------------|----|--------------|-------------|-------|-----------------|
| 0 | baseline (main `c9f94b1` + baseline docs) | — | `b4ea605` | FAIL at `jacocoTestCoverageVerification` (33.2% < 80%) | 68 / 0 failed | 33.2% |
| 1 | `devin/1791491209-modernization-platform-build` | #226 | `79ac2d5` | FAIL at `compileJava` (expected: `src/**` still Boot 2 / javax / Joda; 200 compiler errors) and `spotlessJava` (Gradle 8 implicit-dependency validation — Spotless target `fileTree(rootDir)` includes `build/` outputs; fixed by REST branch's `target 'src/**/*.java'`) | not run | n/a |
