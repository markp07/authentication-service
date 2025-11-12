export interface User {
  email: string;
  userName: string;
  twoFactorEnabled: boolean;
  passkeyEnabled?: boolean;
  emailVerified?: boolean;
  createdAt?: string;
}

