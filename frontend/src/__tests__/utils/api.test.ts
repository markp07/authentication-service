/**
 * @jest-environment jsdom
 */
import { AUTH_API_BASE, WEATHER_API_BASE, fetchWithAuthRetry, getSavedLocations, saveLocation, deleteSavedLocation, reorderSavedLocations } from '../../utils/api';

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

  describe('WEATHER_API_BASE', () => {
    it('should be defined', () => {
      expect(WEATHER_API_BASE).toBeDefined();
      expect(typeof WEATHER_API_BASE).toBe('string');
    });

    it('should be a valid URL', () => {
      expect(WEATHER_API_BASE).toMatch(/^https?:\/\//);
    });
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
});

describe('API functions', () => {
  const mockFetch = jest.fn();
  const originalFetch = global.fetch;

  beforeEach(() => {
    global.fetch = mockFetch;
    mockFetch.mockReset();
  });

  afterEach(() => {
    global.fetch = originalFetch;
  });

  describe('getSavedLocations', () => {
    it('should return locations on success', async () => {
      const mockLocations = [{ id: 1, name: 'Amsterdam' }];
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(mockLocations),
      });

      const result = await getSavedLocations();
      
      expect(result).toEqual(mockLocations);
    });

    it('should throw error on failure', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 500,
      });

      await expect(getSavedLocations()).rejects.toThrow('Failed to fetch saved locations');
    });
  });

  describe('saveLocation', () => {
    it('should save location and return result', async () => {
      const location = { name: 'Amsterdam', latitude: 52.37, longitude: 4.89 };
      const savedLocation = { id: 1, ...location };
      
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        json: () => Promise.resolve(savedLocation),
      });

      const result = await saveLocation(location);
      
      expect(result).toEqual(savedLocation);
      expect(mockFetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(location),
        })
      );
    });

    it('should throw error on failure', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 500,
      });

      await expect(saveLocation({ name: 'Test' })).rejects.toThrow('Failed to save location');
    });
  });

  describe('deleteSavedLocation', () => {
    it('should delete location successfully', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 204,
      });

      await expect(deleteSavedLocation(1)).resolves.toBeUndefined();
      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/saved-locations/1'),
        expect.objectContaining({ method: 'DELETE' })
      );
    });

    it('should throw error on failure', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 500,
      });

      await expect(deleteSavedLocation(1)).rejects.toThrow('Failed to delete saved location');
    });
  });

  describe('reorderSavedLocations', () => {
    it('should reorder locations successfully', async () => {
      const locationIds = [3, 1, 2];
      
      mockFetch.mockResolvedValueOnce({
        ok: true,
        status: 204,
      });

      await expect(reorderSavedLocations(locationIds)).resolves.toBeUndefined();
      expect(mockFetch).toHaveBeenCalledWith(
        expect.stringContaining('/saved-locations/reorder'),
        expect.objectContaining({
          method: 'PUT',
          body: JSON.stringify({ locationIds }),
        })
      );
    });

    it('should throw error on failure', async () => {
      mockFetch.mockResolvedValueOnce({
        ok: false,
        status: 500,
      });

      await expect(reorderSavedLocations([1, 2, 3])).rejects.toThrow('Failed to reorder saved locations');
    });
  });
});
