# Research: Security Remediation

## CSRF Double-Submit Token

- **Decision**: Enable Spring Security CSRF with `CookieCsrfTokenRepository` using `XSRF-TOKEN` cookie and `X-XSRF-TOKEN` header, with cookie attributes `HttpOnly=false`, `SameSite=Lax`, `Secure=true`, `Domain` from existing cookie config, and `Path=/`.
- **Rationale**: Matches the specified double-submit strategy, supports SPA clients, and aligns with existing CSRF endpoint usage.
- **Alternatives considered**: Disabling CSRF or using synchronizer tokens only in server-side templates; rejected due to cookie-based auth and SPA needs.

## CORS Error-Path Handling

- **Decision**: Keep allowlisted CORS for normal responses, remove CORS headers entirely for error responses/paths.
- **Rationale**: Prevents reflection of arbitrary origins and blocks error detail leakage while retaining legitimate cross-origin access.
- **Alternatives considered**: Global allowlist only (still applies on errors) or always returning CORS headers; rejected due to spec requirement.

## JWT Key File Enforcement

- **Decision**: Load RSA keys from file/volume (`jwt.private-key`, `jwt.public-key`) and fail startup with a clear error when missing/invalid; remove silent key generation.
- **Rationale**: Prevents ephemeral signing keys and ensures operational safety in production.
- **Alternatives considered**: Auto-generate keys on missing files or store keys in config; rejected due to security and operational requirements.

## Filter Exclusion Wildcards

- **Decision**: Use Ant-style path matching for `security.excluded-paths` allowlist patterns (e.g., `/api/public/**`).
- **Rationale**: Ensures intended exclusions are matched while default deny is preserved.
- **Alternatives considered**: Exact string match only; rejected because it ignores wildcard patterns.

## Medium/Low Scanner Findings

- **Decision**: Triage each medium/low finding per scanner report and apply fixes in backend security configuration and frontend dependencies/configs.
- **Rationale**: Spec mandates inclusion of medium/low findings within this scope.
- **Alternatives considered**: Deferring medium/low items; rejected due to scope requirements.
