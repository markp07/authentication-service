/**
 * Retry utility for handling 5xx errors with exponential backoff
 * Max 5 retries, total time not exceeding 90 seconds
 */

interface RetryOptions {
  maxRetries?: number;
  maxTotalTime?: number; // in milliseconds
  initialDelay?: number; // in milliseconds
}

/**
 * Calculate delay for exponential backoff
 * Returns delay in milliseconds
 */
function calculateDelay(attempt: number, initialDelay: number): number {
  // Exponential backoff: initialDelay * 2^attempt
  // For attempt 0: 1s, 1: 2s, 2: 4s, 3: 8s, 4: 16s = 31s total
  return initialDelay * Math.pow(2, attempt);
}

/**
 * Check if response has a 5xx server error status
 */
function is5xxError(status: number): boolean {
  return status >= 500 && status < 600;
}

/**
 * Fetch with retry mechanism for 5xx errors
 * Implements exponential backoff with configurable max retries and time limit
 * Note: Always sets credentials to 'include' for cookie-based authentication
 */
export async function fetchWithRetry(
  input: RequestInfo,
  init?: RequestInit,
  options: RetryOptions = {}
): Promise<Response> {
  const {
    maxRetries = 5,
    maxTotalTime = 90000, // 90 seconds
    initialDelay = 1000, // 1 second
  } = options;

  const startTime = Date.now();

  for (let attempt = 0; attempt <= maxRetries; attempt++) {
    try {
      const response = await fetch(input, { ...init, credentials: "include" });
      
      // If not a 5xx error, return the response
      if (!is5xxError(response.status)) {
        return response;
      }

      // If we've exhausted retries, return the error response
      if (attempt >= maxRetries) {
        return response;
      }

      const elapsedTime = Date.now() - startTime;
      const delay = calculateDelay(attempt, initialDelay);
      
      // Check if we have time for another retry
      if (elapsedTime + delay > maxTotalTime) {
        // Not enough time left, return the error response
        return response;
      }

      // Wait before retrying
      await new Promise(resolve => setTimeout(resolve, delay));
    } catch (error) {
      // Network error (connection refused, timeout, abort, etc.) - don't retry
      throw error;
    }
  }

  // Should never reach here due to loop structure, but TypeScript needs this
  throw new Error("Retry logic error");
}

/**
 * Validate auth token with retry mechanism
 * Returns true if token is valid (or was refreshed successfully)
 * Returns false if token is invalid and refresh failed
 */
export async function validateAuthToken(authApiBase: string): Promise<boolean> {
  try {
    // Try to get user info with retry mechanism
    const userResponse = await fetchWithRetry(`${authApiBase}/api/auth/v1/user`);
    
    if (userResponse.ok) {
      return true; // Token is valid
    }
    
    if (userResponse.status === 401) {
      // Token is invalid, try to refresh
      const refreshResponse = await fetchWithRetry(
        `${authApiBase}/api/auth/v1/refresh`,
        { method: "POST" }
      );
      
      if (refreshResponse.ok) {
        // Refresh succeeded, verify by checking user info again
        const verifyResponse = await fetchWithRetry(`${authApiBase}/api/auth/v1/user`);
        return verifyResponse.ok;
      }
      
      // Refresh failed
      return false;
    }
    
    // Other error status (4xx except 401, or 5xx after retries)
    return false;
  } catch (error) {
    // Network error
    return false;
  }
}
