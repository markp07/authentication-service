import React, { useEffect, useState } from "react";
import { AUTH_API_BASE, fetchWithAuthRetry } from "../utils/api";
import { User } from "../types/User";

interface ProfilePageProps {
  onBack: () => void;
  onSecurity: () => void;
  onDeleteAccount: () => void;
  onLogout: () => void;
}

export default function ProfilePage({ onBack, onSecurity, onDeleteAccount, onLogout }: ProfilePageProps) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

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
          <h2 className="text-2xl font-bold">Profile</h2>
        </div>
        {loading ? (
          <div className="text-blue-600">Loading...</div>
        ) : error ? (
          <div className="text-red-600 text-sm bg-red-100 rounded px-2 py-1">{error}</div>
        ) : user ? (
          <div className="mb-6">
            <div className="text-lg">Username: {user.userName}</div>
            <div className="text-gray-600 dark:text-gray-300">E-mail: {user.email}</div>
          </div>
        ) : null}
        <div className="flex flex-col gap-4">
          <button className="bg-blue-600 text-white rounded px-4 py-2 font-semibold" onClick={onSecurity}>Security</button>
          <button className="bg-red-600 text-white rounded px-4 py-2 font-semibold" onClick={onDeleteAccount}>Delete account</button>
          <button className="bg-gray-600 text-white rounded px-4 py-2 font-semibold" onClick={onLogout}>Log out</button>
        </div>
      </div>
    </div>
  );
}
