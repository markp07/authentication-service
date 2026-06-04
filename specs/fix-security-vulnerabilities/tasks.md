---

description: "Task list template for feature implementation"
---

# Tasks: Remediate Security Vulnerabilities

**Input**: Design documents from `/specs/fix-security-vulnerabilities/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Tests are included (requested by plan.md).

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [X] T001 Update plan references in .github/copilot-instructions.md for specs/fix-security-vulnerabilities/plan.md

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T002 Update authentication-service/src/main/resources/application.yaml with required security remediation settings (cors allowlist, security.excluded-paths, jwt key paths, cookie domain/secure)
- [X] T003 [P] Add shared security test helpers in authentication-service/src/test/java/nl/markpost/authentication/security/SecurityTestUtils.java for CSRF/JWT key fixtures

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Enable CSRF Double-Submit Tokens (Priority: P1) 🎯 MVP

**Goal**: Enforce CSRF protection with double-submit tokens and ensure the frontend attaches the required header.

**Independent Test**: `./mvnw test` for CSRF tests and `cd frontend && npm run test` for CSRF header injection.

### Tests for User Story 1 ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [X] T004 [P] [US1] Add CSRF issuance/validation tests in authentication-service/src/test/java/nl/markpost/authentication/config/SecurityConfigCsrfTest.java
- [X] T005 [P] [US1] Update CSRF header injection tests in frontend/src/__tests__/utils/api.test.ts

### Implementation for User Story 1

- [X] T006 [US1] Enable CookieCsrfTokenRepository with required cookie attributes and /v1/csrf issuance in authentication-service/src/main/java/nl/markpost/authentication/config/SecurityConfig.java
- [X] T007 [P] [US1] Update frontend/src/utils/api.ts to read XSRF-TOKEN and attach X-XSRF-TOKEN on POST/PUT/PATCH/DELETE (include optional CSRF bootstrap call)
- [X] T008 [P] [US1] Update frontend/src/utils/retry.ts to preserve CSRF headers on retried requests

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - Harden CORS Error-Path Behavior (Priority: P1)

**Goal**: Keep allowlisted CORS on success responses while stripping CORS headers on error responses.

**Independent Test**: Run CORS tests with `./mvnw test` (focus on error-path coverage).

### Tests for User Story 2 ⚠️

- [X] T009 [P] [US2] Add CORS error-path tests in authentication-service/src/test/java/nl/markpost/authentication/config/SecurityConfigCorsTest.java

### Implementation for User Story 2

- [X] T010 [P] [US2] Add error-response CORS stripping filter in authentication-service/src/main/java/nl/markpost/authentication/filter/CorsErrorHeaderFilter.java
- [X] T011 [US2] Register error-path CORS stripping and allowlist handling in authentication-service/src/main/java/nl/markpost/authentication/config/SecurityConfig.java

**Checkpoint**: User Story 2 should be independently functional and testable

---

## Phase 5: User Story 3 - Enforce JWT Key File Requirements (Priority: P2)

**Goal**: Require JWT keys from configured files and fail startup on missing/invalid keys.

**Independent Test**: `./mvnw test` with JwtKeyProvider tests for missing/invalid keys.

### Tests for User Story 3 ⚠️

- [X] T012 [P] [US3] Add missing/invalid key tests in authentication-service/src/test/java/nl/markpost/authentication/security/JwtKeyProviderTest.java

### Implementation for User Story 3

- [X] T013 [US3] Enforce file-based JWT keys and fail fast on missing/invalid keys in authentication-service/src/main/java/nl/markpost/authentication/security/JwtKeyProvider.java

**Checkpoint**: User Story 3 should be independently functional and testable

---

## Phase 6: User Story 4 - Fix Filter Path Exclusion Matching (Priority: P2)

**Goal**: Use Ant-style allowlist matching for excluded paths with default deny.

**Independent Test**: `./mvnw test` for excluded-path matching behavior.

### Tests for User Story 4 ⚠️

- [X] T014 [P] [US4] Add Ant-style exclusion matching tests in authentication-service/src/test/java/nl/markpost/authentication/config/SecurityConfigExclusionsTest.java

### Implementation for User Story 4

- [X] T015 [US4] Implement Ant-style allowlist matching for security.excluded-paths in authentication-service/src/main/java/nl/markpost/authentication/config/SecurityConfig.java

**Checkpoint**: User Story 4 should be independently functional and testable

---

## Phase 7: User Story 5 - Address Medium/Low Scanner Findings (Priority: P3)

**Goal**: Resolve remaining medium/low security scanner findings across backend and frontend.

**Independent Test**: Run `./mvnw test` and `cd frontend && npm run test` after applying fixes.

### Implementation for User Story 5

- [X] T016 [US5] Apply backend scanner fixes in authentication-service/src/main/java/nl/markpost/authentication/config/SecurityConfig.java and authentication-service/src/main/resources/application.yaml per findings list
- [ ] T017 [US5] Update backend dependencies per scanner findings in pom.xml
- [ ] T018 [US5] Update frontend dependencies/config per scanner findings in frontend/package.json

**Checkpoint**: User Story 5 should be independently functional and testable

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [X] T019 [P] Update specs/fix-security-vulnerabilities/quickstart.md with any final remediation steps
- [X] T020 [P] Add remediation summary to CHANGELOG.md

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3)
- **Polish (Final Phase)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - no dependencies on other stories
- **User Story 2 (P1)**: Can start after Foundational (Phase 2) - no dependencies on other stories
- **User Story 3 (P2)**: Can start after Foundational (Phase 2) - no dependencies on other stories
- **User Story 4 (P2)**: Can start after Foundational (Phase 2) - no dependencies on other stories
- **User Story 5 (P3)**: Can start after Foundational (Phase 2) - depends only on updated findings list

### Within Each User Story

- Tests MUST be written and FAIL before implementation
- Backend security config changes before frontend integration where applicable
- Story complete before moving to next priority

### Parallel Opportunities

- T003 can run in parallel with T002 (different files)
- US1 frontend tasks T007 and T008 can run in parallel
- US2 filter (T010) can run in parallel with test writing (T009)
- US3 tests (T012) can run in parallel with US4 tests (T014)
- US5 dependency updates (T017, T018) can run in parallel
- Polish tasks T019 and T020 can run in parallel

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together:
Task: "Add CSRF issuance/validation tests in authentication-service/src/test/java/nl/markpost/authentication/config/SecurityConfigCsrfTest.java"
Task: "Update CSRF header injection tests in frontend/src/__tests__/utils/api.test.ts"

# Launch frontend implementation tasks together:
Task: "Update frontend/src/utils/api.ts to read XSRF-TOKEN and attach X-XSRF-TOKEN on POST/PUT/PATCH/DELETE (include optional CSRF bootstrap call)"
Task: "Update frontend/src/utils/retry.ts to preserve CSRF headers on retried requests"
```

---

## Parallel Example: User Story 2

```bash
Task: "Add CORS error-path tests in authentication-service/src/test/java/nl/markpost/authentication/config/SecurityConfigCorsTest.java"
Task: "Add error-response CORS stripping filter in authentication-service/src/main/java/nl/markpost/authentication/filter/CorsErrorHeaderFilter.java"
```

---

## Parallel Example: User Story 3

```bash
Task: "Add missing/invalid key tests in authentication-service/src/test/java/nl/markpost/authentication/security/JwtKeyProviderTest.java"
Task: "Implement Ant-style allowlist matching for security.excluded-paths in authentication-service/src/main/java/nl/markpost/authentication/config/SecurityConfig.java"
```

---

## Parallel Example: User Story 5

```bash
Task: "Update backend dependencies per scanner findings in pom.xml"
Task: "Update frontend dependencies/config per scanner findings in frontend/package.json"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test User Story 1 independently
5. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo
4. Add User Story 3 → Test independently → Deploy/Demo
5. Add User Story 4 → Test independently → Deploy/Demo
6. Add User Story 5 → Test independently → Deploy/Demo

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1
   - Developer B: User Story 2
   - Developer C: User Story 3
   - Developer D: User Story 4
   - Developer E: User Story 5
3. Stories complete and integrate independently

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Verify tests fail before implementing
- Stop at any checkpoint to validate story independently
- Avoid vague tasks or same-file conflicts marked as parallel

