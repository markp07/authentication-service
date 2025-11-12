export interface User {
  email: string;
  userName: string;
  twoFactorEnabled: boolean;
  passkeyEnabled?: boolean;
  createdAt?: string;
}

