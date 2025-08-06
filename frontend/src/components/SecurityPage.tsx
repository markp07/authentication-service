import React, { useEffect, useState } from "react";
import { AUTH_API_BASE, fetchWithAuthRetry } from "../utils/api";
import { User } from "../types/User";
import BackupCodesModal from "./BackupCodesModal";
import PasskeyModal from "./PasskeyModal";

interface SecurityPageProps {
  onBack: () => void;
  onChangePassword: () => void;
  onToggle2FA: () => void;
}

export default function SecurityPage({ onBack, onChangePassword, onToggle2FA }: SecurityPageProps) {
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

  return (
    <div className="min-h-screen w-full flex flex-col items-center bg-white dark:bg-gray-900">
      <div className="w-full max-w-md p-6 relative">
        {/* Back button in top-left corner */}
        <button className="absolute top-4 left-4 text-blue-600 font-semibold" onClick={onBack} aria-label="Back">
          ← Back
        </button>
        <div className="flex justify-center items-center mb-6">
          <h2 className="text-2xl font-bold">Security</h2>
        </div>
        {loading ? (
          <div className="text-blue-600">Loading...</div>
        ) : error ? (
          <div className="text-red-600 text-sm bg-red-100 rounded px-2 py-1">{error}</div>
        ) : user ? (
          <div className="mb-6">
            <div className="flex items-center gap-2">
              <span className="font-semibold">2FA Status:</span>
              <span className={user.twoFAEnabled ? "text-green-600" : "text-red-600"}>{user.twoFAEnabled ? "Enabled" : "Disabled"}</span>
            </div>
          </div>
        ) : null}
        <div className="flex flex-col gap-4">
          <button className="bg-blue-600 text-white rounded px-4 py-2 font-semibold" onClick={onChangePassword}>Change password</button>
          <button className="bg-blue-600 text-white rounded px-4 py-2 font-semibold" onClick={onToggle2FA}>{user?.twoFAEnabled ? "Disable 2FA" : "Enable 2FA"}</button>
          <button className="bg-gray-400 text-white rounded px-4 py-2 font-semibold" onClick={() => setShowPasskeyModal(true)}>Configure passkey</button>
          <button
            className={`rounded px-4 py-2 font-semibold ${user?.twoFAEnabled ? "bg-blue-600 text-white" : "bg-gray-400 text-white cursor-not-allowed"}`}
            onClick={() => user?.twoFAEnabled && setShowBackupCodes(true)}
          >
            Generate backup codes
          </button>
        </div>
        <BackupCodesModal open={showBackupCodes} onClose={() => setShowBackupCodes(false)} />
        <PasskeyModal open={showPasskeyModal} onClose={() => setShowPasskeyModal(false)} />
      </div>
    </div>
  );
}
