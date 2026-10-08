# Frontend dependency audit (post-modernization)

Run with Node 22 / npm 10 after the Next 15 / React 19 upgrade:

```bash
source ~/.nvm/nvm.sh && nvm use 22
cd frontend && npm ci && npm audit
```

## Summary

| | Critical | High | Moderate | Low | Total |
|---|---|---|---|---|---|
| Baseline (Next 9.5.1 / React 16 / TS 3.9, see `docs/modernization/baseline/npm-audit.md`) | 10 | 39 | 54 | 6 | **109** |
| After upgrade (this branch) | 0 | 6 | 1 | 0 | **7** |

All 10 critical and 48 of the 54 moderate/low findings are gone; every remaining finding is in a
**dev/build-time** dependency chain, none is in code shipped to the browser.

During the upgrade `vitest` 3.x was flagged (critical: `tinypool` prototype pollution → RCE,
`@vitest/mocker` path traversal). It was bumped to `vitest`/`@vitest/coverage-v8` 5.0.x, which
clears those advisories; `@vitejs/plugin-react` was dropped in favour of Vite's built-in JSX
transform to keep the toolchain small.

## Remaining findings

| Advisory | Severity | Path | Status / justification |
|---|---|---|---|
| GHSA-vfj7-8cjw-p6xm `braces` stack-exhaustion DoS via deeply nested glob patterns | high (×4 entries: `braces`, `micromatch`, `fast-glob`, `@next/eslint-plugin-next`) | `eslint-config-next@15.5.27` → `@next/eslint-plugin-next` → `fast-glob` → `micromatch` → `braces` | **Deferred.** Only reachable while running `npm run lint` on developer machines/CI against patterns we author; not shipped to users. The only upstream fix npm offers is a *downgrade* to `eslint-config-next@14.2.35` (forbidden by the manifest) or `eslint-config-next@16` (requires Next 16). Re-check when a Next 15.5.x patch republishes `@next/eslint-plugin-next` with `fast-glob` ≥ 3.3.4. |
| GHSA-qx2v-qp2m-jg93, GHSA-6g55-p6wh-862q, GHSA-fxqj-rqcc-2cmp, GHSA-r28c-9q8g-f849 `postcss` ≤ 8.5.22 (XSS via unescaped `</style>` in stringify output; arbitrary `.map` file read via attacker-controlled `sourceMappingURL`) | high (1 entry, `postcss`) + moderate (1 entry, `next`) | `next@15.5.27` → `postcss@8.4.31` (pinned by Next) | **Deferred.** PostCSS only runs at `next build` time on CSS we author (`conduit.css`, `styles.css`); no attacker-controlled CSS is processed and no `sourceMappingURL` from untrusted sources is loaded. npm's only fix is `next@16.4.0`, which is outside the Next 15.x target of the modernization manifest. Re-evaluate when Next 15.5.x bumps its pinned `postcss`, or when the orchestrator approves Next 16. |

`npm audit fix` (non-forced) makes no change; `npm audit fix --force` would downgrade
`eslint-config-next` to 14.x and upgrade `next` to 16.x, both of which violate the manifest, so it
was **not** applied.

## Runtime dependencies (shipped to the browser)

| Package | Baseline | Now |
|---|---|---|
| next | 9.5.1 | 15.5.27 |
| react / react-dom | 16.13.1 | 19.3.0 |
| swr | 0.3.0 | 2.5.1 |
| axios | 0.19.2 | 1.20.0 |
| marked | 1.1.1 | 18.1.0 |
| isomorphic-dompurify | – | 4.5.0 (new: sanitizes `marked` output before `dangerouslySetInnerHTML`) |
| lazysizes | 5.2.2 | 5.3.2 |

No advisories are open against any runtime dependency.
