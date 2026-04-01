-- Add account lockout columns to support brute force protection
-- failed_login_attempts: tracks consecutive failed login attempts
-- account_locked_until: when set, the account is locked until this timestamp

ALTER TABLE users ADD COLUMN IF NOT EXISTS failed_login_attempts INTEGER NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN IF NOT EXISTS account_locked_until TIMESTAMP;
