---
name: fullstack-app-testing
description: How to run and E2E-test the RealWorld app (Spring Boot backend + Next.js frontend) through the browser UI, including Java version pitfalls, article/comment UI flows, and non-ASCII text input.
---

# Full-stack E2E testing of ts-java-spring-boot-realworld

## Running the app
- Backend runs on Java 21 (Gradle toolchain in `build.gradle`, `.java-version` = 21). If the shell's default `java` is older, Gradle still resolves the 21 toolchain from `/usr/lib/jvm/java-21-openjdk-amd64`; when in doubt: `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew bootRun`. Java 11 no longer compiles the project (Spring Boot 3.5 / jakarta).
- Backend listens on :8080 and recreates SQLite `dev.db` with seed data (users janedoe/bobsmith/johndoe, several articles) on every bootRun. Ready when `curl http://localhost:8080/tags` returns JSON (startup can take ~1–2 min).
- Frontend: `source ~/.nvm/nvm.sh && nvm use 22 && cd frontend && npm ci && npm run dev` → http://localhost:3000 (Next 15; talks to :8080 via `frontend/lib/utils/constant.ts`, override with `NEXT_PUBLIC_API_URL`). Node 22 LTS is required (`frontend/.nvmrc`); `--openssl-legacy-provider` is no longer needed.

## Auth
- No seed-user password known; the comment box on `/article/<slug>` only renders when logged in. Since the DB is wiped each bootRun, register a fresh user at http://localhost:3000/user/register (any username/email/password) — registration auto-logs you in.

## Frontend quirks to know
- Publishing/updating an article redirects to `/` (home), NOT the article page. To see the article's slug URL, click the article in Global Feed and read the address bar (`/article/<slug>`).
- The home feed is SWR-cached: a just-created/updated article may show stale data until you press F5.
- Seed articles have future-dated timestamps, so new articles may appear BELOW seed articles in Global Feed — scroll down to find them.
- Seeded articles exist (e.g. `/article/testing-spring-boot-applications`), so you usually don't need to create one.
- Edit page is `/editor/<slug>` (reached via "Edit Article" button on the article page).

## Typing non-ASCII (CJK, etc.) text in the GUI
- The computer-use `type` action silently drops CJK characters (xdotool limitation) — the field ends up with only the ASCII parts. Always verify what actually got typed.
- Workaround: put text on the clipboard and paste:
  ```bash
  printf '中文：标题' | DISPLAY=:0 xclip -selection clipboard   # install xclip via apt if missing
  ```
  then in the browser: click field, ctrl+a, ctrl+v. Accented Latin (é, ï, ñ) types fine directly.

## Programmatic input for React forms
- To set very long textarea values (thousands of chars) in the React frontend, don't type: use the native value setter + input event so React state updates:
  `Object.getOwnPropertyDescriptor(HTMLTextAreaElement.prototype,'value').set.call(ta, text); ta.dispatchEvent(new Event('input',{bubbles:true}));`
  Then do boundary transitions (±1 char) with real keystrokes (ctrl+End, type/Backspace) so they're visible on the recording.
- Comments POST via XHR (axios); to prove a disabled submit sends nothing, hook `XMLHttpRequest.prototype.open` and count POSTs to `/comments`.

## API cross-checks
- `curl http://localhost:8080/articles/<slug>` returns the article JSON (slug, title, id) without auth — handy to confirm stored slug/title exactly.
