import { isValidCallback, getSafeCallback } from '../../utils/callbackValidation';

describe('callbackValidation', () => {
  describe('isValidCallback', () => {
    it('should accept relative paths', () => {
      expect(isValidCallback('/')).toBe(true);
      expect(isValidCallback('/home')).toBe(true);
      expect(isValidCallback('/path/to/page')).toBe(true);
      expect(isValidCallback('/path?query=value')).toBe(true);
    });

    it('should reject protocol-relative URLs', () => {
      expect(isValidCallback('//')).toBe(false);
      expect(isValidCallback('//example.com')).toBe(false);
      expect(isValidCallback('//evil.com/path')).toBe(false);
    });

    it('should accept https URLs from trusted domains', () => {
      expect(isValidCallback('https://markpost.dev')).toBe(true);
      expect(isValidCallback('https://markpost.dev/')).toBe(true);
      expect(isValidCallback('https://demo.markpost.dev')).toBe(true);
      expect(isValidCallback('https://weather.markpost.dev')).toBe(true);
      expect(isValidCallback('https://weather.markpost.dev/')).toBe(true);
      expect(isValidCallback('https://weather.markpost.dev/path')).toBe(true);
    });

    it('should accept http URLs only for localhost', () => {
      expect(isValidCallback('http://localhost')).toBe(true);
      expect(isValidCallback('http://localhost:3000')).toBe(true);
      expect(isValidCallback('http://localhost:3000/path')).toBe(true);
    });

    it('should reject http URLs for non-localhost domains', () => {
      expect(isValidCallback('http://markpost.dev')).toBe(false);
      expect(isValidCallback('http://weather.markpost.dev')).toBe(false);
      expect(isValidCallback('http://example.com')).toBe(false);
    });

    it('should reject URLs from untrusted domains', () => {
      expect(isValidCallback('https://evil.com')).toBe(false);
      expect(isValidCallback('https://example.com')).toBe(false);
      expect(isValidCallback('https://fakemarkpost.dev')).toBe(false);
    });

    it('should accept subdomains of trusted domains', () => {
      expect(isValidCallback('https://api.weather.markpost.dev')).toBe(true);
      expect(isValidCallback('https://sub.demo.markpost.dev')).toBe(true);
    });

    it('should reject invalid URLs', () => {
      expect(isValidCallback('javascript:alert(1)')).toBe(false);
      expect(isValidCallback('data:text/html,<script>alert(1)</script>')).toBe(false);
      expect(isValidCallback('not-a-url')).toBe(false);
      expect(isValidCallback('')).toBe(false);
    });

    it('should reject null or undefined', () => {
      expect(isValidCallback(null)).toBe(false);
      expect(isValidCallback(undefined as any)).toBe(false);
    });
  });

  describe('getSafeCallback', () => {
    it('should return callback if valid', () => {
      expect(getSafeCallback('/home')).toBe('/home');
      expect(getSafeCallback('https://weather.markpost.dev')).toBe('https://weather.markpost.dev');
    });

    it('should return default "/" if callback is invalid', () => {
      expect(getSafeCallback('//evil.com')).toBe('/');
      expect(getSafeCallback('https://evil.com')).toBe('/');
      expect(getSafeCallback(null)).toBe('/');
    });

    it('should return custom default if provided', () => {
      expect(getSafeCallback('//evil.com', '/login')).toBe('/login');
      expect(getSafeCallback('https://evil.com', '/home')).toBe('/home');
      expect(getSafeCallback(null, '/custom')).toBe('/custom');
    });
  });
});

