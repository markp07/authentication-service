# Quickstart: Security Remediation

## Prerequisites

- Java 21+, Node.js 20+, Maven, Docker (as per repo README).
- JWT key files available on disk or mounted volume.

## Generate JWT Keys (local/dev)

```bash
./generate-keys.sh
```

Confirm `jwt.private-key` and `jwt.public-key` in `authentication-service/src/main/resources/application.yaml` point to the generated files (or update for your environment).

## CSRF Token Flow (local)

1. Fetch a CSRF token before state-changing requests:
   ```bash
   curl -i http://localhost:12002/api/auth/v1/csrf
   ```
2. Include the `XSRF-TOKEN` cookie value in the `X-XSRF-TOKEN` header for POST/PUT/PATCH/DELETE requests.

## CORS Configuration

- Update `authentication.cors.allowed-origin-patterns` in `application.yaml` to include allowed origins for your environment.

## Run Tests

```bash
./mvnw test
cd frontend && npm run test
```
