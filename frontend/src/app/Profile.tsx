import React, { useEffect, useState } from "react";

interface ProfileProps {
  onClose: () => void;
  onSetup2FA: () => void;
  onChangePassword: () => void;
}

const API_BASE = "http://localhost:12002/v1";

export default function Profile({ onClose, onSetup2FA, onChangePassword }: ProfileProps) {
  const [user, setUser] = useState<any | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function fetchUser() {
      setLoading(true);
      setError(null);
      try {
        const res = await fetch(`${API_BASE}/user`, { credentials: "include" });
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
    <div className="flex flex-col gap-4 min-w-[320px]">
      <h2 className="text-xl font-bold mb-2">Profile</h2>
      {loading ? (
        <div className="text-blue-600">Loading...</div>
      ) : error ? (
        <div className="text-red-600 text-sm bg-red-100 rounded px-2 py-1">{error}</div>
      ) : user ? (
        <div className="bg-blue-50 dark:bg-gray-800 rounded p-4 mb-2">
          <div><span className="font-semibold">Email:</span> {user.email}</div>
          <div><span className="font-semibold">Username:</span> {user.username}</div>
          <div><span className="font-semibold">2FA Enabled:</span> {user.twoFAEnabled ? "Yes" : "No"}</div>
        </div>
      ) : null}
      <button className="bg-gray-600 text-white rounded px-4 py-2 font-semibold" onClick={onClose}>
        Close
      </button>
    </div>
  );
}
