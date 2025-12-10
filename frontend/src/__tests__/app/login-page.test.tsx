/**
 * @jest-environment jsdom
 */
import { renderHook } from '@testing-library/react';
import { useRouter, useSearchParams } from 'next/navigation';
import { isValidCallback } from '../../utils/callbackValidation';

// Mock Next.js navigation hooks
jest.mock('next/navigation', () => ({
  useRouter: jest.fn(),
  useSearchParams: jest.fn(),
}));

describe('Login Page URL Validation', () => {
  const mockPush = jest.fn();
  const mockGet = jest.fn();

  beforeEach(() => {
    (useRouter as jest.Mock).mockReturnValue({ push: mockPush });
    (useSearchParams as jest.Mock).mockReturnValue({ get: mockGet });
    mockPush.mockReset();
    mockGet.mockReset();
  });

  describe('Callback URL validation', () => {
    it('should reject absolute URLs from untrusted domains', () => {
      const callback = 'https://evil.com/steal-data';
      
      expect(isValidCallback(callback)).toBe(false);
    });

    it('should accept absolute URLs from trusted domains', () => {
      const callback = 'https://weather.markpost.dev/';

      expect(isValidCallback(callback)).toBe(true);
    });

    it('should reject protocol-relative URLs', () => {
      const callback = '//evil.com/steal-data';
      
      expect(isValidCallback(callback)).toBe(false);
    });

    it('should accept valid relative paths', () => {
      const validPaths = [
        '/profile',
        '/dashboard',
        '/settings/security',
        '/api/data',
      ];

      validPaths.forEach(callback => {
        expect(isValidCallback(callback)).toBe(true);
      });
    });

    it('should reject paths without leading slash', () => {
      const callback = 'profile';
      
      expect(isValidCallback(callback)).toBe(false);
    });

    it('should reject javascript: protocol', () => {
      const callback = 'javascript:alert(1)';
      
      expect(isValidCallback(callback)).toBe(false);
    });

    it('should reject data: protocol', () => {
      const callback = 'data:text/html,<script>alert(1)</script>';
      
      expect(isValidCallback(callback)).toBe(false);
    });
  });
});
