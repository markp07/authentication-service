# CSRF Token Endpoint

## What Is It?

The `GET /v1/csrf` endpoint generates a cryptographically secure CSRF token and makes it available to frontend applications via both a cookie and the response body.

This endpoint implements the **Double Submit Cookie** pattern — a stateless CSRF protection strategy that does not require server-side session storage. The auth service acts as the trusted token issuer; any backend service in the same domain can validate the CSRF protection by comparing the cookie value to the header/body value supplied by the client.

The endpoint is **public** — it can be called by both anonymous visitors and authenticated users.

---

## How It Works

1. A **32-byte cryptographically secure random token** is generated using `SecureRandom` and encoded as URL-safe Base64 (no padding).
2. The token is set on the HTTP response as a **non-HttpOnly cookie** named `csrf_token`:
   - `Secure` — transmitted over HTTPS only (configurable via `cookie.secure`)
   - `Domain` — scoped to the configured base domain, making it readable by all subdomains (configurable via `cookie.domain`)
   - `Path=/`
   - `Max-Age=3600` (1 hour)
   - **Not** `HttpOnly` — JavaScript must be able to read this cookie to include the token in request headers
3. The same token value is also returned in the JSON response body.

```
GET /v1/csrf

HTTP/1.1 200 OK
Set-Cookie: csrf_token=<token>; Domain=.yourdomain.tld; Path=/; Max-Age=3600; Secure; SameSite=Lax
Content-Type: application/json

{
  "token": "<token>"
}
```

---

## Flow Diagram

```mermaid
sequenceDiagram
    actor User
    participant FE as Frontend App
    participant Auth as Auth Service<br/>(GET /v1/csrf)
    participant API as Backend Service

    User->>FE: Load page / trigger action

    FE->>Auth: GET /v1/csrf
    Auth-->>FE: 200 OK<br/>Body: { "token": "abc123..." }<br/>Set-Cookie: csrf_token=abc123...; Domain=.yourdomain.tld

    Note over FE: Token available via<br/>response body or JS cookie read

    FE->>API: POST /api/some-endpoint<br/>Header: X-CSRF-Token: abc123...<br/>Cookie: csrf_token=abc123...

    Note over API: Validate CSRF:<br/>cookie value == header value?

    alt Token valid
        API-->>FE: 200 OK — request processed
    else Token missing or mismatch
        API-->>FE: 403 Forbidden
    end
```

---

## How a Consuming Application Uses It

### Step 1 — Fetch the CSRF Token

Call the endpoint before performing any state-mutating request (e.g. on app initialisation or just before form submission):

```typescript
// TypeScript / fetch API
const response = await fetch('https://auth.yourdomain.tld/api/auth/v1/csrf', {
  credentials: 'include', // ensures the cookie is stored
});
const { token } = await response.json();
```

> **Note:** `credentials: 'include'` is required so the browser stores the `csrf_token` cookie set by the auth service.

### Step 2 — Include the Token in Subsequent Requests

Send the token value in the `X-CSRF-Token` request header (or as a body field) when calling your backend service. The browser will automatically send the `csrf_token` cookie alongside the request.

```typescript
await fetch('https://api.yourdomain.tld/some-endpoint', {
  method: 'POST',
  credentials: 'include',
  headers: {
    'Content-Type': 'application/json',
    'X-CSRF-Token': token,  // value from Step 1
  },
  body: JSON.stringify({ /* your payload */ }),
});
```

### Step 3 — Validate in Your Backend Service

Your backend service receives both the `csrf_token` cookie (sent automatically by the browser) and the `X-CSRF-Token` header (set explicitly by the frontend). It must verify these two values match:

```java
// Example Spring Boot filter / interceptor
String cookieValue = Arrays.stream(request.getCookies())
    .filter(c -> "csrf_token".equals(c.getName()))
    .map(Cookie::getValue)
    .findFirst()
    .orElse(null);

String headerValue = request.getHeader("X-CSRF-Token");

if (cookieValue == null || !cookieValue.equals(headerValue)) {
    response.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF token mismatch");
    return;
}
```

> **Why this works:** A cross-origin attacker can trigger the browser to send the `csrf_token` cookie automatically, but cannot read its value (same-origin policy). Without being able to read the cookie, the attacker cannot set a matching `X-CSRF-Token` header — so the validation fails.

---

## Token Refresh

CSRF tokens expire after **1 hour**. Frontend applications should re-fetch the token if:
- The page has been open longer than an hour without any CSRF-protected request
- The backend returns `403 Forbidden` with a CSRF-related error

A simple approach is to call `GET /v1/csrf` once during application initialisation, store the token in memory, and re-fetch it only on expiry or error.

---

## Configuration Reference

| Property | Default | Description |
|---|---|---|
| `cookie.secure` | `true` | Sets the `Secure` flag on the CSRF cookie. Set to `false` for local HTTP development. |
| `cookie.domain` | `yourdomain.tld` | Domain scope of the cookie. All subdomains can read the cookie. |

These properties are shared with the authentication cookies (`access_token`, `refresh_token`) and are configured in `application.yaml`.
