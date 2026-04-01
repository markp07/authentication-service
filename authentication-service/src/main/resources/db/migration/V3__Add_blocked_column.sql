-- Add is_blocked column to users table for admin account blocking
ALTER TABLE users ADD COLUMN is_blocked BOOLEAN NOT NULL DEFAULT FALSE;
