// Centralized API base URL and fetchWithAuthRetry utility

export const isDev = typeof window !== "undefined" && window.location.hostname === "localhost";
export const AUTH_API_BASE = isDev
  ? (process.env.NEXT_PUBLIC_API_URL || "http://localhost:12002")
  : "https://demo.markpost.dev";
export const WEATHER_API_BASE = isDev
  ? (process.env.NEXT_PUBLIC_WEATHER_API_URL || "http://localhost:12001")
  : "https://demo.markpost.dev";

/**
 * Generic fetch utility that retries on 401 by refreshing the token, then retries the original request.
 * If refresh also fails with 401, redirects to login.
 */
export async function fetchWithAuthRetry(input: RequestInfo, init?: RequestInit): Promise<Response> {
  let res = await fetch(input, { ...init, credentials: "include" });
  if (res.status !== 401) return res;

  // Try to refresh token
  const refreshRes = await fetch(`${AUTH_API_BASE}/api/auth/v1/refresh`, { method: "POST", credentials: "include" });
  if (refreshRes.status === 401) {
    // Redirect to login
    if (typeof window !== "undefined") {
      window.location.href = "/login";
    }
    throw new Error("Session expired. Redirecting to login.");
  }
  // Retry original request
  res = await fetch(input, { ...init, credentials: "include" });
  return res;
}

/**
 * Saved Locations API functions
 */

export async function getSavedLocations() {
  const res = await fetchWithAuthRetry(`${WEATHER_API_BASE}/api/weather/v1/saved-locations`);
  if (!res.ok) {
    throw new Error("Failed to fetch saved locations");
  }
  return res.json();
}

export async function saveLocation(location: any) {
  const res = await fetchWithAuthRetry(`${WEATHER_API_BASE}/api/weather/v1/saved-locations`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(location),
  });
  if (!res.ok) {
    throw new Error("Failed to save location");
  }
  return res.json();
}

export async function deleteSavedLocation(id: number) {
  const res = await fetchWithAuthRetry(`${WEATHER_API_BASE}/api/weather/v1/saved-locations/${id}`, {
    method: "DELETE",
  });
  if (!res.ok && res.status !== 204) {
    throw new Error("Failed to delete saved location");
  }
}

export async function reorderSavedLocations(locationIds: number[]) {
  const res = await fetchWithAuthRetry(`${WEATHER_API_BASE}/api/weather/v1/saved-locations/reorder`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ locationIds }),
  });
  if (!res.ok && res.status !== 204) {
    throw new Error("Failed to reorder saved locations");
  }
}

