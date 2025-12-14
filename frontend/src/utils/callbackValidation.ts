/**
 * List of trusted domains for callback URLs
 * The base domain is read from environment variable, defaults to 'yourdomain.tld'
 */
const TRUSTED_DOMAINS = [
  process.env.NEXT_PUBLIC_BASE_DOMAIN || 'yourdomain.tld',
  'localhost'
];

/**
 * Validates if a callback URL is safe to redirect to.
 * Allows:
 * - Relative paths starting with "/" but not "//"
 * - Absolute URLs from trusted domains
 *
 * @param callback The callback URL to validate
 * @returns true if the callback is valid and safe, false otherwise
 */
export function isValidCallback(callback: string | null): boolean {
  if (!callback) return false;

  // Allow relative paths (but not protocol-relative URLs)
  if (callback.startsWith("/") && !callback.startsWith("//")) {
    return true;
  }

  // Check if it's an absolute URL from a trusted domain
  try {
    const url = new URL(callback);

    // Only allow https protocol (or http for localhost in development)
    if (url.protocol !== 'https:' && !(url.protocol === 'http:' && url.hostname === 'localhost')) {
      return false;
    }

    // Check if the hostname or its parent domain is in the trusted list
    const hostname = url.hostname;
    return TRUSTED_DOMAINS.some(domain => {
      // Exact match
      if (hostname === domain) return true;
      // Subdomain match (e.g., api.weather.yourdomain.tld matches yourdomain.tld)
      if (hostname.endsWith('.' + domain)) return true;
      return false;
    });
  } catch {
    // Invalid URL format
    return false;
  }
}

/**
 * Gets a safe callback URL, or returns the default if invalid
 *
 * @param callback The callback URL to validate
 * @param defaultUrl The default URL to return if callback is invalid (default: "/")
 * @returns The callback if valid, otherwise the default URL
 */
export function getSafeCallback(callback: string | null, defaultUrl: string = "/"): string {
  return isValidCallback(callback) ? callback! : defaultUrl;
}

