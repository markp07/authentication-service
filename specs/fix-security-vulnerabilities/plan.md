# Implementation Plan: Remediate Security Vulnerabilities

**Branch**: `1235-update-security-remediation` | **Date**: 2026-06-04 | **Spec**: [./spec.md](./spec.md)

**Input**: Feature specification from `/specs/fix-security-vulnerabilities/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Remediate critical/high vulnerabilities in CSRF, CORS, JWT key handling, and filter exclusions, plus all medium/low scanner findings for the backend (`authentication-service/`) and frontend (`frontend/`). The work enforces double-submit CSRF tokens, tightens CORS behavior (no headers on error paths), requires JWT keys from a file/volume with startup failure on missing keys, and uses Ant-style path matching for allowlisted filter exclusions.

## Technical Context

**Language/Version**: Java 21 (Spring Boot 3.5.6), TypeScript 6.0.3 (Next.js 16.2.6)

**Primary Dependencies**: Spring Security, Spring WebMVC, JJWT, Springdoc OpenAPI, Next.js, React 19.2.6

**Storage**: PostgreSQL, Redis, filesystem-mounted JWT key files

**Testing**: JUnit 5 + Spring Boot Test/Security Test, Jest

**Target Platform**: Linux server (backend), modern browsers (frontend)

**Project Type**: Web application (Spring Boot backend + Next.js frontend)

**Performance Goals**: Not specified; maintain current performance characteristics.

**Constraints**: No secrets in repo; JWT keys must be provided via file/volume; CORS allowlist must be config-driven; allowlist-only filter exclusions.

**Scale/Scope**: Not specified in the feature spec.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Gate | Status | Justification |
|---|---|---|---|
| **Security-First** | Changes must strengthen security posture. | ✅ PASS | Remediation work closes known vulnerabilities and adds guardrails. |
| **Clear API Contracts** | Document and preserve client-visible behavior. | ⚠️ WARN | CSRF enforcement and CORS changes alter request expectations; contracts will be documented. |
| **Test-First & Automated Tests** | Behavioral changes include tests (including failure cases). | ✅ PASS | Plan includes unit/integration tests for security flows. |
| **Backward Compatibility & Minimal Changes** | Prefer non-breaking changes. | ⚠️ WARN | CSRF and JWT key enforcement require coordinated client updates. |
| **No-Secrets & Operational Safety** | No secrets committed; safe operational defaults. | ✅ PASS | Key files stay external and startup fails if missing. |

## Project Structure

### Documentation (this feature)

```text
specs/fix-security-vulnerabilities/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
authentication-service/
├── src/main/java/nl/markpost/authentication/config/SecurityConfig.java
├── src/main/java/nl/markpost/authentication/security/JwtKeyProvider.java
├── src/main/java/nl/markpost/authentication/filter/               # security filters (CORS/CSRF adjustments if needed)
├── src/main/resources/application.yaml
└── src/test/java/nl/markpost/authentication/                       # security config/JWT key tests

frontend/
├── src/utils/api.ts
├── src/utils/retry.ts
├── src/__tests__/utils/api.test.ts
└── package.json
```

**Structure Decision**: Use the existing backend/frontend split; all changes stay within the security configuration, key management, and API utilities.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|---|---|---|
| Clear API Contracts | CSRF token requirements and CORS behavior change client expectations. | Continuing without CSRF/CORS fixes leaves critical vulnerabilities open. |
| Backward Compatibility | JWT key enforcement requires provisioning key files and client CSRF updates. | Ephemeral keys and disabled CSRF are unacceptable security risks. |

---
## Phase 0: Outline & Research

1. **CSRF double-submit implementation (Spring Security + Next.js)**
   - Task: Identify Spring Security configuration for `XSRF-TOKEN` cookie + `X-XSRF-TOKEN` header.
   - Task: Confirm cookie attributes and header handling in fetch utilities.
2. **CORS error-path behavior**
   - Task: Determine how to strip CORS headers on error responses while preserving allowlist on success paths.
3. **JWT key file enforcement**
   - Task: Validate key loading patterns that fail fast on missing or invalid key files.
4. **Ant-style filter exclusions**
   - Task: Confirm Ant path matcher usage for allowlisted exclusions.
5. **Medium/low scanner findings**
   - Task: Enumerate outstanding medium/low findings in backend and frontend and map each to a concrete fix.

**Output**: `research.md` with decisions and rationales for all items.

## Phase 1: Design & Contracts

**Prerequisites:** `research.md` complete

1. **Data Model (`data-model.md`)**
   - Document configuration inputs and no persistent data model changes.
2. **Interface Contracts (`/contracts/`)**
   - Define CSRF token exchange (cookie + header), CORS response rules, JWT key file requirements, and filter exclusion matching.
3. **Quickstart (`quickstart.md`)**
   - Document local key generation, CSRF token usage, and configuration knobs.
4. **Agent Context Update**
   - Update `.github/copilot-instructions.md` to reference this plan.

**Output**: `data-model.md`, `contracts/security-remediation.md`, `quickstart.md`, updated `.github/copilot-instructions.md`.

## Phase 2: Implementation Plan

### Backend (authentication-service/)

1. **Enable CSRF with double-submit tokens**
   - Update `SecurityConfig` to enable CSRF using `CookieCsrfTokenRepository` and `X-XSRF-TOKEN` header support.
   - Configure cookie attributes: `HttpOnly=false`, `SameSite=Lax`, `Secure=true`, `Domain` from existing cookie config, `Path=/`.
   - Ensure `/v1/csrf` continues to issue the token for SPA bootstrapping.
2. **Harden CORS behavior**
   - Keep allowlist from `authentication.cors.allowed-origin-patterns` for normal responses.
   - Add logic to strip CORS headers on error paths/responses (e.g., dedicated filter/exception handler).
3. **Enforce JWT key files**
   - Update `JwtKeyProvider` to fail startup with a clear error if key paths are missing/invalid.
   - Remove silent key generation; require file/volume to be present.
4. **Fix filter path exclusions**
   - Replace direct string matching with Ant-style matching (e.g., `AntPathMatcher`/`PathPatternParser`).
   - Enforce allowlist-only exclusions and default deny for other paths.
5. **Medium/low findings**
   - Apply scanner-reported fixes (e.g., tighten headers/cookies, dependency updates) in `SecurityConfig`, `application.yaml`, and supporting classes.

### Frontend (frontend/)

1. **Attach CSRF headers for state-changing requests**
   - Update `src/utils/api.ts` (and helpers in `src/utils/retry.ts`) to read `XSRF-TOKEN` cookie and send `X-XSRF-TOKEN` header on POST/PUT/PATCH/DELETE.
   - Add a lightweight CSRF bootstrap call if needed when the app loads.
2. **Medium/low findings**
   - Address scanner findings (dependency upgrades in `package.json`, lint/config changes if required by the findings).

### Tests

- **Backend**: Add/extend unit/integration tests under `authentication-service/src/test/java/nl/markpost/authentication/` for:
  - CSRF token issuance and rejection without header.
  - CORS allowlist behavior and no CORS headers on error responses.
  - JWT key provider failing when keys are missing/invalid.
  - Ant-style exclusion matching for allowed paths only.
- **Frontend**: Update/extend tests in `frontend/src/__tests__/utils/api.test.ts` to assert CSRF header injection and retry behavior.

**Commands**:
- Backend: `./mvnw test`
- Frontend: `cd frontend && npm run test`
