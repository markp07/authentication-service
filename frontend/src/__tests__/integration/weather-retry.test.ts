/**
 * @jest-environment jsdom
 * 
 * Integration test to verify weather fetching with retry mechanism
 */
import { fetchWithRetry } from '../../utils/retry';

describe('Weather Retry Integration', () => {
  const mockFetch = jest.fn();
  const originalFetch = global.fetch;
  const WEATHER_API_BASE = 'http://localhost:12001';

  beforeEach(() => {
    global.fetch = mockFetch;
    mockFetch.mockReset();
  });

  afterEach(() => {
    global.fetch = originalFetch;
  });

  describe('Weather forecast endpoint with retry', () => {
    const weatherUrl = `${WEATHER_API_BASE}/api/weather/v1/forecast?latitude=52.37&longitude=4.89`;

    it('should return weather data on successful request', async () => {
      const mockWeather = {
        location: 'Amsterdam',
        current: { temperature: 15, weatherCode: '1', windSpeed: 10, windDirection: 'N', time: '2024-01-01T12:00:00Z' },
        hourly: [],
        daily: [],
      };

      mockFetch.mockResolvedValueOnce({
        status: 200,
        ok: true,
        json: () => Promise.resolve(mockWeather),
      });

      const response = await fetchWithRetry(weatherUrl);
      
      expect(response.ok).toBe(true);
      expect(response.status).toBe(200);
      expect(mockFetch).toHaveBeenCalledTimes(1);
      expect(mockFetch).toHaveBeenCalledWith(weatherUrl, expect.objectContaining({ credentials: 'include' }));
    });

    it('should retry on 500 error and eventually succeed', async () => {
      const mockWeather = {
        location: 'Amsterdam',
        current: { temperature: 15, weatherCode: '1', windSpeed: 10, windDirection: 'N', time: '2024-01-01T12:00:00Z' },
        hourly: [],
        daily: [],
      };

      // First call returns 500
      mockFetch.mockResolvedValueOnce({
        status: 500,
        ok: false,
        statusText: 'Internal Server Error',
      });

      // Second call succeeds
      mockFetch.mockResolvedValueOnce({
        status: 200,
        ok: true,
        json: () => Promise.resolve(mockWeather),
      });

      const response = await fetchWithRetry(weatherUrl);
      
      expect(response.ok).toBe(true);
      expect(response.status).toBe(200);
      expect(mockFetch).toHaveBeenCalledTimes(2);
    });

    it('should retry on 503 error and eventually succeed', async () => {
      const mockWeather = {
        location: 'Amsterdam',
        current: { temperature: 15, weatherCode: '1', windSpeed: 10, windDirection: 'N', time: '2024-01-01T12:00:00Z' },
        hourly: [],
        daily: [],
      };

      // First call returns 503 (Service Unavailable)
      mockFetch.mockResolvedValueOnce({
        status: 503,
        ok: false,
        statusText: 'Service Unavailable',
      });

      // Second call succeeds
      mockFetch.mockResolvedValueOnce({
        status: 200,
        ok: true,
        json: () => Promise.resolve(mockWeather),
      });

      const response = await fetchWithRetry(weatherUrl);
      
      expect(response.ok).toBe(true);
      expect(response.status).toBe(200);
      expect(mockFetch).toHaveBeenCalledTimes(2);
    });

    it('should retry up to 5 times on persistent 5xx errors', async () => {
      // All 6 calls (initial + 5 retries) return 500
      const errorResponse = {
        status: 500,
        ok: false,
        statusText: 'Internal Server Error',
      };
      for (let i = 0; i < 6; i++) {
        mockFetch.mockResolvedValueOnce(errorResponse);
      }

      const response = await fetchWithRetry(weatherUrl);
      
      expect(response.ok).toBe(false);
      expect(response.status).toBe(500);
      expect(mockFetch).toHaveBeenCalledTimes(6); // Initial + 5 retries
    }, 35000); // 35 seconds timeout

    it('should not exceed 90 second total time limit', async () => {
      const errorResponse = {
        status: 500,
        ok: false,
        statusText: 'Internal Server Error',
      };
      mockFetch.mockResolvedValue(errorResponse);

      const startTime = Date.now();
      const response = await fetchWithRetry(weatherUrl);
      const elapsedTime = Date.now() - startTime;
      
      expect(response.status).toBe(500);
      // Should have stopped before exceeding 90 second limit
      // With exponential backoff (1+2+4+8+16 = 31s), total time should be around 31s
      expect(elapsedTime).toBeLessThan(90000);
      expect(elapsedTime).toBeGreaterThan(30000); // Should have taken at least 31s
    }, 95000); // 95 seconds timeout for test

    it('should not retry on 401 authentication error', async () => {
      mockFetch.mockResolvedValueOnce({
        status: 401,
        ok: false,
        statusText: 'Unauthorized',
      });

      const response = await fetchWithRetry(weatherUrl);
      
      expect(response.status).toBe(401);
      expect(mockFetch).toHaveBeenCalledTimes(1); // No retry on 401
    });

    it('should not retry on 404 not found error', async () => {
      mockFetch.mockResolvedValueOnce({
        status: 404,
        ok: false,
        statusText: 'Not Found',
      });

      const response = await fetchWithRetry(weatherUrl);
      
      expect(response.status).toBe(404);
      expect(mockFetch).toHaveBeenCalledTimes(1); // No retry on 404
    });

    it('should handle multiple 5xx errors before success', async () => {
      const mockWeather = {
        location: 'Amsterdam',
        current: { temperature: 15, weatherCode: '1', windSpeed: 10, windDirection: 'N', time: '2024-01-01T12:00:00Z' },
        hourly: [],
        daily: [],
      };

      // First two calls return 500, third succeeds
      mockFetch.mockResolvedValueOnce({
        status: 502,
        ok: false,
        statusText: 'Bad Gateway',
      });
      mockFetch.mockResolvedValueOnce({
        status: 503,
        ok: false,
        statusText: 'Service Unavailable',
      });
      mockFetch.mockResolvedValueOnce({
        status: 200,
        ok: true,
        json: () => Promise.resolve(mockWeather),
      });

      const response = await fetchWithRetry(weatherUrl);
      
      expect(response.ok).toBe(true);
      expect(response.status).toBe(200);
      expect(mockFetch).toHaveBeenCalledTimes(3); // 1 initial + 2 retries
    });
  });

  describe('Weather retry with exponential backoff timing', () => {
    const weatherUrl = `${WEATHER_API_BASE}/api/weather/v1/forecast?latitude=52.37&longitude=4.89`;

    it('should use exponential backoff delays', async () => {
      jest.useFakeTimers();
      
      const errorResponse = { status: 500, ok: false };
      const successResponse = { status: 200, ok: true, json: () => Promise.resolve({}) };
      
      mockFetch
        .mockResolvedValueOnce(errorResponse)
        .mockResolvedValueOnce(errorResponse)
        .mockResolvedValueOnce(successResponse);

      const fetchPromise = fetchWithRetry(weatherUrl, undefined, {
        initialDelay: 1000, // 1 second
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
      expect(result.status).toBe(200);

      jest.useRealTimers();
    });
  });
});
