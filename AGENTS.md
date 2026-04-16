# AGENTS.md

Machine instructions for GitHub Copilot and other AI coding agents.
This file is optimized for automated execution, not human onboarding.

## 0. Mission

Implement requested changes safely with minimal scope in this repository.
Priorities: security > correctness > compatibility > clarity > speed.

## 1. Repository Facts (source of truth)

- Monorepo root contains:
  - `authentication-service/` (Spring Boot backend)
  - `frontend/` (Next.js TypeScript frontend)
  - `docker-compose.yml` (local stack)
- Backend build: Maven wrapper at repo root (`./mvnw`), module defined in root `pom.xml`.
- Frontend scripts from `frontend/package.json`:
  - `npm run dev`
  - `npm run build`
  - `npm run lint`
  - `npm run test`
- Environment template: `.env.example`.

## 2. Hard Rules

- Do not commit secrets or private keys.
- Do not print or persist token/credential values in logs, tests, or docs.
- Deny access to `.env` and other secret-bearing env files (`.env`, `.env.local`, `.env.*`) except `.env.example`.
- Do not weaken auth/security behavior unless explicitly requested.
- Do not edit generated/build outputs:
  - `**/target/**`
  - `**/.next/**`
  - `**/coverage/**`
- Do not modify lock-infra unless asked:
  - `docker-compose.yml`
  - build scripts (`build-and-up.sh`, `start.sh`, `update.sh`)
- Keep changes minimal and task-focused.
- Preserve API compatibility unless user requests breaking change.

## 3. Security-Sensitive Areas

Treat all changes touching these as high-risk:

- JWT and refresh flow
- Password reset/change
- 2FA/TOTP and backup codes
- WebAuthn/passkeys
- CORS/CSRF/cookies/headers
- User deletion and identity updates

For high-risk changes, explicitly validate negative paths and failure handling.

## 4. Edit Protocol

1. Read nearby files before editing.
2. Reuse existing patterns and naming.
3. Prefer smallest possible patch.
4. Add concise comments only for non-obvious logic.
5. Avoid broad refactors mixed with feature/fix work.
6. If requirement is ambiguous, state assumptions in output.

## 5. Backend Protocol (`authentication-service/`)

- Keep controller -> service -> repository layering consistent.
- Validate request DTOs and return safe errors.
- For schema changes, add Flyway migration under:
  - `authentication-service/src/main/resources/db/migration/`
- Never remove/rename migration files that may have run in other environments.
- Respect OpenAPI-generated contract patterns; avoid manual drift.

### Backend Commands

Run from repository root unless task requires otherwise.

```bash
./mvnw test
```

```bash
./mvnw clean verify
```

If a task only touches backend module internals, targeted test runs are preferred first.

## 6. Frontend Protocol (`frontend/`)

- Use TypeScript strictness-compatible code.
- Keep UI strings localizable via `frontend/messages/*.json` when adding text.
- Reuse existing utilities/components before creating new abstractions.
- Keep auth state and error handling explicit; avoid silent failures.

### Frontend Commands

```bash
cd frontend
npm run lint
npm run test
```

Use targeted tests first when possible, then broader suite if needed.

## 7. Testing Policy

- Run checks for every changed area unless impossible in current environment.
- Prefer targeted tests for speed, then broaden if risk is high.
- If checks are not run, explicitly state:
  - what was not run
  - why
  - exact command to run

## 8. Response Contract for Agents

For non-trivial work, output in this order:

1. `Plan` (short checklist)
2. `Changes` (files + intent)
3. `Validation` (commands + result)
4. `Risks/Assumptions` (only if applicable)
5. `Next Steps` (optional numbered list)

Additional requirements:

- Reference files with backticks and explicit paths.
- Be concise and deterministic.
- Do not claim success without validation evidence.

## 9. Command Preference

Prefer these tools/commands in this order:

1. Repository wrappers (`./mvnw`)
2. Package scripts (`npm run ...`)
3. Docker Compose for local infra (`docker compose ...`)

Avoid ad-hoc global tools when project-local commands exist.

## 10. Safe Defaults

- Default to backward-compatible behavior.
- Default to deny-by-default for security posture.
- Default to explicit null/error handling at boundaries.
- Default to small PR-sized patches.

## 11. Branching Rules (release-aware)

- Allowed long-lived base branches: `main` and `master`.
- Use exactly one of these PR source branch prefixes:
  - `major/<ticket-or-topic>` -> semantic version **major** bump intent
  - `feature/<ticket-or-topic>` -> semantic version **minor** bump intent
  - `fix/<ticket-or-topic>` -> semantic version **patch** bump intent
- Use lowercase kebab-case after the slash.
- Keep branch names deterministic and short; avoid spaces and uppercase.
- Backward-compat note: `bugfix/*` may still be interpreted as patch by release automation, but new work should use `fix/*`.

Branch name regex:

```regex
^(major|feature|fix)\/[a-z0-9][a-z0-9-]*$
```

## 12. Standardized Commit Message Framework

Use Conventional Commits with strict format:

```text
<type>(<scope>): <subject>

[optional body]

[optional footers]
```

Rules:

- Allowed `type`: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `build`, `ci`, `perf`, `revert`.
- `scope` is recommended and should match subsystem (examples: `auth`, `2fa`, `passkey`, `frontend`, `maven`, `docker`).
- Subject must be imperative, lowercase start, and no trailing period.
- Do not include secrets/tokens in body or footers.
- For breaking changes, include `BREAKING CHANGE:` footer.

Branch-to-commit intent mapping:

- `major/*`: at least one merged commit should indicate a breaking change (`!` in header and/or `BREAKING CHANGE:` footer).
- `feature/*`: prefer `feat(...)` commits.
- `fix/*`: prefer `fix(...)` commits; `chore(maven)`, `chore(npm)`, and `chore(docker)` are allowed for dependency/infrastructure patch releases.

Commit header regex:

```regex
^(feat|fix|docs|refactor|test|chore|build|ci|perf|revert)(\([a-z0-9-]+\))?(!)?: [^\s].*[^\.]$
```

## 13. Machine-Readable Policy File

- Normative schema file: `agents.schema.json` at repository root.
- Agents should validate policy objects against this schema before generating automated metadata.
- If schema and prose conflict, follow schema for machine validation and report the conflict.

## 14. Versioning Rules

- All policy and automation version strings must use Semantic Versioning: `MAJOR.MINOR.PATCH`.
- Canonical format example: `1.0.0`.
- Semver regex:

```regex
^[0-9]+\.[0-9]+\.[0-9]+$
```
