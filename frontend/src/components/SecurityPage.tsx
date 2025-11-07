import React, { useEffect, useState } from "react";
import { AUTH_API_BASE, fetchWithAuthRetry } from "../utils/api";
import { User } from "../types/User";
import BackupCodesModal from "./BackupCodesModal";
import PasskeyModal from "./PasskeyModal";
import { IconShield, IconKey, IconLock, IconFingerprint, IconCheck, IconX, IconAlertTriangle } from "@tabler/icons-react";

interface SecurityPageProps {
  onChangePassword: () => void;
  onToggle2FA: () => void;
}

export default function SecurityPage({ onChangePassword, onToggle2FA }: SecurityPageProps) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showBackupCodes, setShowBackupCodes] = useState(false);
  const [showPasskeyModal, setShowPasskeyModal] = useState(false);

  useEffect(() => {
    async function fetchUser() {
      setLoading(true);
      setError(null);
      try {
        const res = await fetchWithAuthRetry(`${AUTH_API_BASE}/api/auth/v1/user`);
        if (res.ok) {
          setUser(await res.json());
        } else {
          setError("Failed to load user profile.");
        }
      } catch {
        setError("Network error.");
      }
      setLoading(false);
    }
    fetchUser();
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-full">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-6">
        <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg p-4">
          <p className="text-red-600 dark:text-red-400">{error}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6 max-w-4xl">
      <div>
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white">Security Settings</h1>
        <p className="text-gray-600 dark:text-gray-400 mt-2">Manage your account security and authentication methods</p>
      </div>

      {/* Password Section */}
      <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6">
        <div className="flex items-start gap-4">
          <div className="w-12 h-12 bg-blue-100 dark:bg-blue-900/30 rounded-lg flex items-center justify-center flex-shrink-0">
            <IconLock size={24} className="text-blue-600 dark:text-blue-400" />
          </div>
          <div className="flex-1">
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">Password</h3>
            <p className="text-sm text-gray-600 dark:text-gray-400 mb-4">
              Change your password to keep your account secure
            </p>
            <button
              onClick={onChangePassword}
              className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium transition-colors"
            >
              Change Password
            </button>
          </div>
        </div>
      </div>

      {/* Two-Factor Authentication Section */}
      <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6">
        <div className="flex items-start gap-4">
          <div className={`w-12 h-12 rounded-lg flex items-center justify-center flex-shrink-0 ${
            user?.twoFactorEnabled
              ? "bg-green-100 dark:bg-green-900/30"
              : "bg-gray-100 dark:bg-gray-700"
          }`}>
            {user?.twoFactorEnabled ? (
              <IconCheck size={24} className="text-green-600 dark:text-green-400" />
            ) : (
              <IconShield size={24} className="text-gray-600 dark:text-gray-400" />
            )}
          </div>
          <div className="flex-1">
            <div className="flex items-center gap-2 mb-2">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white">Two-Factor Authentication</h3>
              <span className={`px-2 py-1 text-xs font-medium rounded-full ${
                user?.twoFactorEnabled
                  ? "bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400"
                  : "bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300"
              }`}>
                {user?.twoFactorEnabled ? "Enabled" : "Disabled"}
              </span>
            </div>
            <p className="text-sm text-gray-600 dark:text-gray-400 mb-4">
              {user?.twoFactorEnabled
                ? "Add an extra layer of security to your account with TOTP authentication"
                : "Protect your account with time-based one-time passwords (TOTP)"}
            </p>
            <div className="flex flex-wrap gap-2">
              <button
                onClick={onToggle2FA}
                className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                  user?.twoFactorEnabled
                    ? "bg-red-600 hover:bg-red-700 text-white"
                    : "bg-blue-600 hover:bg-blue-700 text-white"
                }`}
              >
                {user?.twoFactorEnabled ? "Disable 2FA" : "Enable 2FA"}
              </button>
              {user?.twoFactorEnabled && (
                <button
                  onClick={() => setShowBackupCodes(true)}
                  className="px-4 py-2 bg-gray-200 hover:bg-gray-300 dark:bg-gray-700 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300 rounded-lg font-medium transition-colors"
                >
                  Generate Backup Codes
                </button>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Passkeys Section */}
      <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6">
        <div className="flex items-start gap-4">
          <div className="w-12 h-12 bg-purple-100 dark:bg-purple-900/30 rounded-lg flex items-center justify-center flex-shrink-0">
            <IconFingerprint size={24} className="text-purple-600 dark:text-purple-400" />
          </div>
          <div className="flex-1">
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">Passkeys</h3>
            <p className="text-sm text-gray-600 dark:text-gray-400 mb-4">
              Use biometric authentication or security keys for passwordless login
            </p>
            <button
              onClick={() => setShowPasskeyModal(true)}
              className="px-4 py-2 bg-purple-600 hover:bg-purple-700 text-white rounded-lg font-medium transition-colors"
            >
              Manage Passkeys
            </button>
          </div>
        </div>
      </div>

      {/* Security Tips */}
      <div className="bg-amber-50 dark:bg-amber-900/20 border border-amber-200 dark:border-amber-800 rounded-xl p-6">
        <div className="flex items-start gap-4">
          <IconAlertTriangle size={24} className="text-amber-600 dark:text-amber-400 flex-shrink-0 mt-1" />
          <div>
            <h3 className="text-lg font-semibold text-amber-900 dark:text-amber-200 mb-2">Security Tips</h3>
            <ul className="space-y-2 text-sm text-amber-800 dark:text-amber-300">
              <li className="flex items-start gap-2">
                <span className="text-amber-600 dark:text-amber-400 mt-0.5">•</span>
                <span>Enable two-factor authentication for enhanced security</span>
              </li>
              <li className="flex items-start gap-2">
                <span className="text-amber-600 dark:text-amber-400 mt-0.5">•</span>
                <span>Use a strong, unique password for your account</span>
              </li>
              <li className="flex items-start gap-2">
                <span className="text-amber-600 dark:text-amber-400 mt-0.5">•</span>
                <span>Keep your backup codes in a secure location</span>
              </li>
              <li className="flex items-start gap-2">
                <span className="text-amber-600 dark:text-amber-400 mt-0.5">•</span>
                <span>Consider using passkeys for passwordless authentication</span>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <BackupCodesModal open={showBackupCodes} onClose={() => setShowBackupCodes(false)} />
      <PasskeyModal open={showPasskeyModal} onClose={() => setShowPasskeyModal(false)} />
    </div>
  );
}
