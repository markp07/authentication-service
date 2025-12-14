// Centralized API base URL and fetchWithAuthRetry utility

export const isDev = typeof window !== "undefined" && window.location.hostname === "localhost";
export const AUTH_API_BASE = isDev ? "http://localhost:12002" : (process.env.NEXT_PUBLIC_API_URL || "https://auth.yourdomain.tld");

/**
 * Generic fetch utility that retries on 401 by refreshing the token, then retries the original request.
 * If refresh also fails with 401, redirects to login with callback URL.
 */
export async function fetchWithAuthRetry(input: RequestInfo, init?: RequestInit): Promise<Response> {
  let res = await fetch(input, { ...init, credentials: "include" });
  if (res.status !== 401) return res;

  // Try to refresh token
  const refreshRes = await fetch(`${AUTH_API_BASE}/api/auth/v1/refresh`, { method: "POST", credentials: "include" });
  if (refreshRes.status === 401) {
    // Redirect to login with callback URL
    if (typeof window !== "undefined") {
      const currentPath = window.location.pathname;
      window.location.href = `/login?callback=${encodeURIComponent(currentPath)}`;
    }
    throw new Error("Session expired. Redirecting to login.");
  }
  // Retry original request
  res = await fetch(input, { ...init, credentials: "include" });
  return res;
}


