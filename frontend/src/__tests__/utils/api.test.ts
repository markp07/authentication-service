/**
 * @jest-environment jsdom
 */
import { AUTH_API_BASE, fetchWithAuthRetry } from '../../utils/api';

describe('API Utils', () => {
  describe('AUTH_API_BASE', () => {
    it('should be defined', () => {
      expect(AUTH_API_BASE).toBeDefined();
      expect(typeof AUTH_API_BASE).toBe('string');
    });

    it('should be a valid URL', () => {
      expect(AUTH_API_BASE).toMatch(/^https?:\/\//);
    });
  });

  describe('fetchWithAuthRetry', () => {
    const mockFetch = jest.fn();
    const originalFetch = global.fetch;
    const originalConsoleError = console.error;

    beforeEach(() => {
      global.fetch = mockFetch;
      mockFetch.mockReset();

      // Suppress jsdom navigation errors in tests
      console.error = jest.fn((...args) => {
        // Check if any of the arguments contain the navigation error message
        const hasNavigationError = args.some(arg => {
          if (arg instanceof Error) {
            return arg.message.includes('Not implemented: navigation');
          }
          if (typeof arg === 'string') {
            return arg.includes('Not implemented: navigation');
          }
          if (arg && typeof arg === 'object' && 'message' in arg) {
            return arg.message?.includes('Not implemented: navigation');
          }
          return false;
        });

        if (hasNavigationError) {
          return;
        }

        originalConsoleError(...args);
      });
    });

    afterEach(() => {
      global.fetch = originalFetch;
      console.error = originalConsoleError;
    });

    it('should return response directly if status is not 401', async () => {
      const mockResponse = { status: 200, ok: true };
      mockFetch.mockResolvedValueOnce(mockResponse);

      const result = await fetchWithAuthRetry('/test-url');

      expect(result).toBe(mockResponse);
      expect(mockFetch).toHaveBeenCalledTimes(1);
    });

    it('should retry after refreshing token on 401', async () => {
      // First call returns 401
      mockFetch.mockResolvedValueOnce({ status: 401, ok: false });
      // Refresh call succeeds
      mockFetch.mockResolvedValueOnce({ status: 200, ok: true });
      // Retry call succeeds
      const successResponse = { status: 200, ok: true };
      mockFetch.mockResolvedValueOnce(successResponse);

      const result = await fetchWithAuthRetry('/test-url');

      expect(result).toBe(successResponse);
      expect(mockFetch).toHaveBeenCalledTimes(3);
      // Verify refresh endpoint was called
      expect(mockFetch).toHaveBeenNthCalledWith(2, `${AUTH_API_BASE}/api/auth/v1/refresh`, expect.any(Object));
    });

    it('should throw error if refresh also returns 401', async () => {
      // First call returns 401
      mockFetch.mockResolvedValueOnce({ status: 401, ok: false });
      // Refresh call also returns 401
      mockFetch.mockResolvedValueOnce({ status: 401, ok: false });

      await expect(fetchWithAuthRetry('/test-url')).rejects.toThrow('Session expired');
      // Note: window.location.href change happens but we can't easily test it in jsdom
    });
  });
});

