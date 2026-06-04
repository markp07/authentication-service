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

## Security Verification Checks

1. **CSRF enforcement**: send a POST without `X-XSRF-TOKEN` and confirm `403`.
2. **CORS error-path behavior**: trigger an error response and confirm `Access-Control-Allow-Origin` is absent.
3. **JWT key enforcement**: start with missing `jwt.private-key`/`jwt.public-key` paths and confirm startup fails with a clear message.

## Run Tests

```bash
./mvnw test
cd frontend && npm run test
```
