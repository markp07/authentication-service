<!--
Sync Impact Report

Version change: none -> 1.0.0
Modified principles:
- [PRINCIPLE_1_NAME] -> Security-First (NON-NEGOTIABLE)
- [PRINCIPLE_2_NAME] -> Clear API Contracts
- [PRINCIPLE_3_NAME] -> Test-First & Automated Tests
- [PRINCIPLE_4_NAME] -> Backward Compatibility & Minimal Changes
- [PRINCIPLE_5_NAME] -> No-Secrets & Operational Safety
Added sections:
- Responsibilities
- Security-Sensitive Areas
- Development Workflow & Edit Protocol
Removed sections: none
Templates requiring updates:
- .specify/templates/plan-template.md: ✅ aligned
- .specify/templates/spec-template.md: ✅ aligned
- .specify/templates/tasks-template.md: ✅ aligned
- .specify/templates/commands/*.md: ⚠ pending (no commands folder)
Follow-up TODOs: none
-->

# Authentication Service Constitution

## Core Principles

### Security-First (NON-NEGOTIABLE)
All code and changes MUST preserve or strengthen the security posture. Any change touching
authentication, token handling, session management, or secrets is high-risk and requires
explicit justification, targeted tests (including negative/failure cases), and a security
review before merge.

Rationale: This service is the authentication source of truth; failures or regressions
have wide blast radius across downstream services.

### Clear API Contracts
Public endpoints and token formats MUST be documented and stable. API changes that
affect clients MUST include a migration plan and tests proving backward compatibility
or a documented breaking-change migration path.

Rationale: Other applications depend on JWTs and refresh behavior; accidental contract
changes break downstream services.

### Test-First & Automated Tests
Every behavioral change MUST include tests. High-risk areas (JWT, refresh, 2FA, passkeys,
password reset) MUST have unit and integration tests that exercise success and failure
paths. Tests should be runnable locally with the project's existing commands.

Rationale: Authentication correctness is critical and must be validated automatically.

### Backward Compatibility & Minimal Changes
Prefer non-breaking changes. If a breaking change is necessary, it MUST be introduced on a
`major/*` branch and include explicit migration steps, feature flags, or dual-support
periods as appropriate.

Rationale: Preserve availability for downstream services and enable coordinated rollout.

### No-Secrets & Operational Safety
Never commit secrets, private keys, or credentials. `.env.example` is the only allowed
env template in the repo; `.env` and similar files MUST be excluded from version control.
Logs, tests, and docs MUST NOT contain sensitive material or token values.

Rationale: Prevent accidental disclosure and align with AGENTS.md hard rules.

## Responsibilities

- Scope: This repository implements a two-part application:
  - `authentication-service/` — Spring Boot backend (Maven)
  - `frontend/` — Next.js TypeScript frontend
- Primary responsibilities:
  - Provide authentication flows: register, login (username/password, TOTP, passkey),
	logout, recovery (password reset / account recovery), email verification.
  - Issue and rotate JWT access tokens and manage refresh tokens used by downstream
	services for authN/authZ.
  - Expose user management endpoints (create, verify email, update username, delete).

Security ownership: Any change touching JWTs, refresh logic, TOTP, passkeys, password
reset flows, or storage of credentials is the responsibility of authors to validate and
the reviewers to require additional security tests and a brief threat-model note in PRs.

## Security-Sensitive Areas (high-risk)

- JWT issuance and validation (signing keys, key rotation, algorithm choices)
- Refresh token rotation and revocation semantics
- Password storage and reset/change flows
- 2FA/TOTP setup, verification, and backup codes
- WebAuthn / passkey registration and authentication
- CORS, CSRF, secure cookie flags, and header-based protections
- User deletion, identity updates, and related data retention

Developers MUST add unit and integration tests for any code touching these areas and
demonstrate failure handling (expired/invalid tokens, revoked refresh, replay attempts).

## Development Workflow & Edit Protocol

- Run commands (project root):
  - Backend build/run: `./mvnw` (use project wrapper)
  - Frontend dev: `cd frontend && npm run dev`
  - Backend tests: `./mvnw test`
  - Frontend tests: `cd frontend && npm run test`

- Edit protocol (per AGENTS.md):
  1. Read nearby files before editing and follow existing patterns.
  2. Keep changes minimal and focused; avoid unrelated refactors in the same PR.
  3. For schema changes add Flyway migrations under
	 `authentication-service/src/main/resources/db/migration/`.
  4. Do not commit secrets; use `.env.example` as the template and keep actual secrets
	 out of the repository.

- Branch and commit rules (per AGENTS.md):
  - Branch prefixes indicate semantic intent: `major/*`, `feature/*`, `fix/*`.
  - Commit messages MUST follow Conventional Commits and the project's regex rules
	(see `AGENTS.md` for exact header regex). Use scopes like `auth`, `2fa`, `passkey`,
	`frontend` where applicable.

## Testing Expectations

- Unit tests for logic; integration tests for end-to-end flows involving JWTs, refresh,
  2FA, and passkeys.
- High-risk changes MUST include negative-path tests (expired token, invalid OTP,
  revoked refresh token, challenge replay).
- Running tests locally is required before opening PRs: backend `./mvnw test`, frontend
  `cd frontend && npm run test`.

## Governance

- Amendment procedure: Amendments to this constitution are recorded here and require a
  PR describing the change, the rationale, test plan, and a migration plan if any.
  Approval requires at least one maintainer review and passing CI checks. Major
  governance changes that redefine principles are a MAJOR version bump.

- Versioning policy:
  - Semantic versioning applied to the constitution: MAJOR.MINOR.PATCH
  - MAJOR for incompatible governance changes; MINOR for added principles/sections;
	PATCH for clarifications or typo fixes.

- Compliance review: Any PR touching security-sensitive areas MUST include a short
  compliance checklist in the PR description referencing this constitution and the
  relevant tests.

**Version**: 1.0.0 | **Ratified**: 2026-06-04 | **Last Amended**: 2026-06-04

