# Data Model: Security Remediation

## Overview

No persistent database schema changes are required for this remediation. Changes are limited to security configuration and runtime key management.

## Configuration Inputs

| Setting | Location | Purpose | Validation/Notes |
|---|---|---|---|
| `authentication.cors.allowed-origin-patterns` | `authentication-service/src/main/resources/application.yaml` | Allowlist for CORS origins on success paths | Must be explicit patterns; no wildcard origin reflection on errors. |
| `security.excluded-paths` | `application.yaml` | Allowlisted unauthenticated endpoints | Evaluated with Ant-style matching; default deny. |
| `jwt.private-key` / `jwt.public-key` | `application.yaml` | File/volume paths for RSA keys | Startup fails when missing or invalid; no auto-generation. |
| `cookie.domain` / `cookie.secure` | `application.yaml` | Domain/secure flags for cookies | Used to set CSRF cookie attributes (domain, secure flag). |

## State/Transitions

- JWT key loading transitions: **missing/invalid keys → startup failure** (no fallback generation).
- CSRF token issuance: **GET /v1/csrf → XSRF-TOKEN cookie set**.
