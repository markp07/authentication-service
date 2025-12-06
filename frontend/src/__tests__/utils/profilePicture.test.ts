import { generateProfilePictureUrl } from '../../utils/profilePicture';

describe('generateProfilePictureUrl', () => {
  it('should generate a valid Dicebear URL with base64 encoded username', () => {
    const username = 'testuser';
    const url = generateProfilePictureUrl(username);
    
    // Should contain the Dicebear API base URL
    expect(url).toContain('https://api.dicebear.com/9.x/identicon/svg');
    
    // Should contain a seed parameter
    expect(url).toContain('seed=');
  });

  it('should generate different URLs for different usernames', () => {
    const url1 = generateProfilePictureUrl('user1');
    const url2 = generateProfilePictureUrl('user2');
    
    expect(url1).not.toBe(url2);
  });

  it('should handle special characters in username', () => {
    const username = 'test@user.com';
    const url = generateProfilePictureUrl(username);
    
    // Should still generate a valid URL
    expect(url).toContain('https://api.dicebear.com/9.x/identicon/svg');
    expect(url).toContain('seed=');
  });

  it('should handle empty username', () => {
    const url = generateProfilePictureUrl('');
    
    // Should still generate a valid URL (empty string base64 encoded)
    expect(url).toContain('https://api.dicebear.com/9.x/identicon/svg');
  });

  it('should URL encode the seed parameter', () => {
    const username = 'test+user';
    const url = generateProfilePictureUrl(username);
    
    // The URL should be properly encoded (no raw + in URL)
    expect(url).not.toMatch(/seed=[^&]*\+/);
  });
});
