/**
 * @jest-environment jsdom
 */
import { fetchWithRetry, validateAuthToken } from '../../utils/retry';

describe('Retry Utility', () => {
  const mockFetch = jest.fn();
  const originalFetch = global.fetch;

  beforeEach(() => {
    global.fetch = mockFetch;
    mockFetch.mockReset();
    jest.clearAllTimers();
  });

  afterEach(() => {
    global.fetch = originalFetch;
  });

  describe('fetchWithRetry', () => {
    it('should return response immediately if status is not 5xx', async () => {
      const mockResponse = { status: 200, ok: true };
      mockFetch.mockResolvedValueOnce(mockResponse);

      const result = await fetchWithRetry('/test-url');
      
      expect(result).toBe(mockResponse);
      expect(mockFetch).toHaveBeenCalledTimes(1);
    });

    it('should return response immediately if status is 401', async () => {
      const mockResponse = { status: 401, ok: false };
      mockFetch.mockResolvedValueOnce(mockResponse);

      const result = await fetchWithRetry('/test-url');
      
      expect(result).toBe(mockResponse);
      expect(mockFetch).toHaveBeenCalledTimes(1);
    });

    it('should retry on 500 error', async () => {
      // First call returns 500
      mockFetch.mockResolvedValueOnce({ status: 500, ok: false });
      // Second call succeeds
      const successResponse = { status: 200, ok: true };
      mockFetch.mockResolvedValueOnce(successResponse);

      const result = await fetchWithRetry('/test-url');
      
      expect(result).toBe(successResponse);
      expect(mockFetch).toHaveBeenCalledTimes(2);
    });

    it('should retry on 503 error', async () => {
      // First call returns 503
      mockFetch.mockResolvedValueOnce({ status: 503, ok: false });
      // Second call succeeds
      const successResponse = { status: 200, ok: true };
      mockFetch.mockResolvedValueOnce(successResponse);

      const result = await fetchWithRetry('/test-url');
      
      expect(result).toBe(successResponse);
      expect(mockFetch).toHaveBeenCalledTimes(2);
    });

    it('should retry max 5 times and return last error response', async () => {
      // All 6 calls (initial + 5 retries) return 500
      const errorResponse = { status: 500, ok: false };
      for (let i = 0; i < 6; i++) {
        mockFetch.mockResolvedValueOnce(errorResponse);
      }

      const result = await fetchWithRetry('/test-url');
      
      expect(result).toBe(errorResponse);
      expect(mockFetch).toHaveBeenCalledTimes(6); // Initial + 5 retries
    }, 35000); // 35 seconds timeout (1+2+4+8+16 = 31s + buffer)

    it('should respect maxTotalTime limit', async () => {
      const errorResponse = { status: 500, ok: false };
      mockFetch.mockResolvedValue(errorResponse);

      const startTime = Date.now();
      // Set very short time limit (500ms) and initial delay (200ms)
      const result = await fetchWithRetry('/test-url', undefined, {
        maxTotalTime: 500,
        initialDelay: 200,
      });

      const elapsedTime = Date.now() - startTime;
      
      expect(result.status).toBe(500);
      // Should have stopped before exceeding time limit
      expect(elapsedTime).toBeLessThan(1000);
      // Should have tried at least once
      expect(mockFetch).toHaveBeenCalled();
    });

    it('should throw on network error without retry', async () => {
      mockFetch.mockRejectedValueOnce(new Error('Network error'));

      await expect(fetchWithRetry('/test-url')).rejects.toThrow('Network error');
      expect(mockFetch).toHaveBeenCalledTimes(1);
    });

    it('should use exponential backoff for retries', async () => {
      jest.useFakeTimers();
      
      const errorResponse = { status: 500, ok: false };
      const successResponse = { status: 200, ok: true };
      
      mockFetch
        .mockResolvedValueOnce(errorResponse)
        .mockResolvedValueOnce(errorResponse)
        .mockResolvedValueOnce(successResponse);

      const fetchPromise = fetchWithRetry('/test-url', undefined, {
        initialDelay: 1000,
      });

      // First call is immediate
      await Promise.resolve();
      expect(mockFetch).toHaveBeenCalledTimes(1);

      // First retry after 1s (2^0 * 1000)
      await jest.advanceTimersByTimeAsync(1000);
      await Promise.resolve();
      expect(mockFetch).toHaveBeenCalledTimes(2);

      // Second retry after 2s (2^1 * 1000)
      await jest.advanceTimersByTimeAsync(2000);
      await Promise.resolve();
      expect(mockFetch).toHaveBeenCalledTimes(3);

      const result = await fetchPromise;
      expect(result).toBe(successResponse);

      jest.useRealTimers();
    });
  });

  describe('validateAuthToken', () => {
    const AUTH_API_BASE = 'http://localhost:12002';

    it('should return true if token is valid', async () => {
      mockFetch.mockResolvedValueOnce({ status: 200, ok: true });

      const result = await validateAuthToken(AUTH_API_BASE);
      
      expect(result).toBe(true);
      expect(mockFetch).toHaveBeenCalledTimes(1);
      expect(mockFetch).toHaveBeenCalledWith(
        `${AUTH_API_BASE}/api/auth/v1/user`,
        expect.any(Object)
      );
    });

    it('should return true if token is invalid but refresh succeeds', async () => {
      // First call returns 401
      mockFetch.mockResolvedValueOnce({ status: 401, ok: false });
      // Refresh call succeeds
      mockFetch.mockResolvedValueOnce({ status: 200, ok: true });
      // Verify call succeeds
      mockFetch.mockResolvedValueOnce({ status: 200, ok: true });

      const result = await validateAuthToken(AUTH_API_BASE);
      
      expect(result).toBe(true);
      expect(mockFetch).toHaveBeenCalledTimes(3);
      // Verify refresh endpoint was called
      expect(mockFetch).toHaveBeenNthCalledWith(
        2,
        `${AUTH_API_BASE}/api/auth/v1/refresh`,
        expect.objectContaining({ method: 'POST' })
      );
    });

    it('should return false if token is invalid and refresh fails', async () => {
      // First call returns 401
      mockFetch.mockResolvedValueOnce({ status: 401, ok: false });
      // Refresh call fails with 401
      mockFetch.mockResolvedValueOnce({ status: 401, ok: false });

      const result = await validateAuthToken(AUTH_API_BASE);
      
      expect(result).toBe(false);
      expect(mockFetch).toHaveBeenCalledTimes(2);
    });

    it('should retry on 5xx errors', async () => {
      // First call returns 500
      mockFetch.mockResolvedValueOnce({ status: 500, ok: false });
      // Second call succeeds
      mockFetch.mockResolvedValueOnce({ status: 200, ok: true });

      const result = await validateAuthToken(AUTH_API_BASE);
      
      expect(result).toBe(true);
      expect(mockFetch).toHaveBeenCalledTimes(2);
    });

    it('should return false after exhausting retries on 5xx errors', async () => {
      // All calls return 500
      const errorResponse = { status: 500, ok: false };
      for (let i = 0; i < 6; i++) {
        mockFetch.mockResolvedValueOnce(errorResponse);
      }

      const result = await validateAuthToken(AUTH_API_BASE);
      
      expect(result).toBe(false);
      expect(mockFetch).toHaveBeenCalledTimes(6); // Initial + 5 retries
    }, 35000); // 35 seconds timeout (1+2+4+8+16 = 31s + buffer)

    it('should handle 5xx error during refresh and retry', async () => {
      // First call returns 401
      mockFetch.mockResolvedValueOnce({ status: 401, ok: false });
      // Refresh call returns 500
      mockFetch.mockResolvedValueOnce({ status: 500, ok: false });
      // Retry refresh call succeeds
      mockFetch.mockResolvedValueOnce({ status: 200, ok: true });
      // Verify call succeeds
      mockFetch.mockResolvedValueOnce({ status: 200, ok: true });

      const result = await validateAuthToken(AUTH_API_BASE);
      
      expect(result).toBe(true);
      expect(mockFetch).toHaveBeenCalledTimes(4);
    });

    it('should return false on network error', async () => {
      mockFetch.mockRejectedValueOnce(new Error('Network error'));

      const result = await validateAuthToken(AUTH_API_BASE);
      
      expect(result).toBe(false);
      expect(mockFetch).toHaveBeenCalledTimes(1);
    });

    it('should return false on other 4xx errors', async () => {
      mockFetch.mockResolvedValueOnce({ status: 403, ok: false });

      const result = await validateAuthToken(AUTH_API_BASE);
      
      expect(result).toBe(false);
      expect(mockFetch).toHaveBeenCalledTimes(1);
    });
  });
});
