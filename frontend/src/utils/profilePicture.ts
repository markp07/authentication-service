/**
 * Generate a profile picture URL using the Dicebear API
 * @param username - The username to use as seed (will be base64 encoded)
 * @returns The URL to the profile picture SVG
 */
export function generateProfilePictureUrl(username: string): string {
  // Base64 encode the username to use as seed
  const seed = Buffer.from(username).toString('base64');
  return `https://api.dicebear.com/9.x/identicon/svg?seed=${seed}`;
}
