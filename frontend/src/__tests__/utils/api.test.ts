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

    beforeEach(() => {
      global.fetch = mockFetch;
      mockFetch.mockReset();
    });

    afterEach(() => {
      global.fetch = originalFetch;
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
      // Mock window.location
      delete (window as any).location;
      (window as any).location = { href: '' };

      // First call returns 401
      mockFetch.mockResolvedValueOnce({ status: 401, ok: false });
      // Refresh call also returns 401
      mockFetch.mockResolvedValueOnce({ status: 401, ok: false });

      await expect(fetchWithAuthRetry('/test-url')).rejects.toThrow('Session expired');
      expect(window.location.href).toContain('/login?callback=');
    });
  });
});

