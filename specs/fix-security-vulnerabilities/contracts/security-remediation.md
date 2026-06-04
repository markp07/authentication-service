# Contract: Security Remediation Behaviors

## CSRF Token Contract

- **Token issuance**: `GET /v1/csrf` issues a `XSRF-TOKEN` cookie.
- **Cookie attributes**: `HttpOnly=false`, `SameSite=Lax`, `Secure=true`, `Domain=<cookie.domain>`, `Path=/`.
- **Client requirement**: State-changing requests (POST/PUT/PATCH/DELETE) must send `X-XSRF-TOKEN` header matching the cookie value.
- **Failure mode**: Missing/invalid header yields 403 response.

## CORS Contract

- **Allowlist**: Success responses include CORS headers only for origins matching `authentication.cors.allowed-origin-patterns`.
- **Error paths**: Error responses must not include CORS headers (no origin reflection).

## JWT Key Contract

- **Key source**: RSA keys loaded from `jwt.private-key` and `jwt.public-key` file paths.
- **Startup behavior**: Missing/invalid key files cause startup failure with a clear error message.
- **No auto-generation**: Keys are never generated silently.

## Filter Exclusion Contract

- **Matching**: Excluded paths use Ant-style matching (e.g., `/api/public/**`).
- **Scope**: Allowlist only; all non-matching paths remain protected.
