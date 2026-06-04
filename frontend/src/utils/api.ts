// Centralized API base URL and fetchWithAuthRetry utility

export const isDev = typeof window !== "undefined" && window.location.hostname === "localhost";
const CSRF_COOKIE_NAME = "XSRF-TOKEN";
const CSRF_HEADER_NAME = "X-XSRF-TOKEN";
const MUTATING_METHODS = new Set(["POST", "PUT", "PATCH", "DELETE"]);

// Get API URL from runtime config if available, otherwise from build-time env, or default
function getApiUrl(): string {
  if (typeof window !== "undefined") {
    // Client-side: check window.__ENV__ first (injected at runtime)
    const runtimeApiUrl = window.__ENV__?.NEXT_PUBLIC_API_URL;
    if (runtimeApiUrl) return runtimeApiUrl;
  }
  // Fallback to build-time env or default
  return process.env.NEXT_PUBLIC_API_URL || "https://auth.yourdomain.tld";
}

export const AUTH_API_BASE = isDev ? "http://localhost:12002" : getApiUrl();

function getCookieValue(name: string): string | undefined {
  if (typeof document === "undefined") {
    return undefined;
  }

  const cookie = document.cookie
    .split(";")
    .map((part) => part.trim())
    .find((part) => part.startsWith(`${name}=`));

  if (!cookie) {
    return undefined;
  }

  return decodeURIComponent(cookie.substring(name.length + 1));
}

function isStateChangingRequest(init?: RequestInit): boolean {
  const method = (init?.method ?? "GET").toUpperCase();
  return MUTATING_METHODS.has(method);
}

async function ensureCsrfToken(): Promise<string | undefined> {
  const existingToken = getCookieValue(CSRF_COOKIE_NAME);
  if (existingToken) {
    return existingToken;
  }

  await fetch(`${AUTH_API_BASE}/api/auth/v1/csrf`, { method: "GET", credentials: "include" });
  return getCookieValue(CSRF_COOKIE_NAME);
}

async function withCsrfHeaders(init?: RequestInit): Promise<RequestInit | undefined> {
  if (!isStateChangingRequest(init)) {
    return init;
  }

  const token = await ensureCsrfToken();
  if (!token) {
    return init;
  }

  const requestInit = { ...(init ?? {}) };
  const headers = new Headers(requestInit.headers ?? {});
  headers.set(CSRF_HEADER_NAME, token);
  requestInit.headers = headers;
  return requestInit;
}

/**
 * Generic fetch utility that retries on 401 by refreshing the token, then retries the original request.
 * If refresh also fails with 401, redirects to login with callback URL.
 */
export async function fetchWithAuthRetry(input: RequestInfo, init?: RequestInit): Promise<Response> {
  const requestInit = await withCsrfHeaders(init);
  let res = await fetch(input, { ...requestInit, credentials: "include" });
  if (res.status !== 401) return res;

  // Try to refresh token
  const refreshRequestInit = await withCsrfHeaders({ method: "POST" });
  const refreshRes = await fetch(`${AUTH_API_BASE}/api/auth/v1/refresh`, {
    ...refreshRequestInit,
    credentials: "include",
  });
  if (refreshRes.status === 401) {
    // Redirect to login with callback URL
    if (typeof window !== "undefined") {
      const currentPath = window.location.pathname;
      window.location.href = `/login?callback=${encodeURIComponent(currentPath)}`;
    }
    throw new Error("Session expired. Redirecting to login.");
  }
  // Retry original request
  const retryInit = await withCsrfHeaders(init);
  res = await fetch(input, { ...retryInit, credentials: "include" });
  return res;
}


