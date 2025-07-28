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
  const [editing, setEditing] = useState(false);
  const [username, setUsername] = useState("");
  const [usernameError, setUsernameError] = useState<string | null>(null);
  const [usernameSuccess, setUsernameSuccess] = useState<string | null>(null);

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

  useEffect(() => {
    if (user) setUsername(user.username || "");
  }, [user]);

  async function handleUsernameSave() {
    setUsernameError(null);
    setUsernameSuccess(null);
    if (!username.trim()) {
      setUsernameError("Username cannot be empty.");
      return;
    }
    try {
      const res = await fetch(`${API_BASE}/user`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ username }),
      });
      if (res.ok) {
        setUsernameSuccess("Username updated successfully.");
        setEditing(false);
        setUser({ ...user, username });
      } else {
        setUsernameError("Failed to update username.");
      }
    } catch {
      setUsernameError("Network error.");
    }
  }

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
          <div className="flex items-center gap-2">
            <span className="font-semibold">Username:</span>
            {editing ? (
              <>
                <input
                  className="border rounded px-2 py-1 text-sm bg-white dark:bg-gray-900"
                  value={username}
                  onChange={e => setUsername(e.target.value)}
                  autoFocus
                />
                <button className="bg-green-600 text-white rounded px-2 py-1 text-xs font-semibold" onClick={handleUsernameSave} type="button">Save</button>
                <button className="bg-gray-400 text-white rounded px-2 py-1 text-xs font-semibold" onClick={() => { setEditing(false); setUsername(user.username); }} type="button">Cancel</button>
              </>
            ) : (
              <>
                <span>{user.username}</span>
                <button className="ml-2 text-xs text-blue-600 underline" onClick={() => setEditing(true)} type="button">Edit</button>
              </>
            )}
          </div>
          {usernameError && <div className="text-red-600 text-xs mt-1">{usernameError}</div>}
          {usernameSuccess && <div className="text-green-600 text-xs mt-1">{usernameSuccess}</div>}
          <div><span className="font-semibold">2FA Enabled:</span> {user.twoFAEnabled ? "Yes" : "No"}</div>
        </div>
      ) : null}
      <button className="bg-gray-600 text-white rounded px-4 py-2 font-semibold" onClick={onClose}>
        Close
      </button>
    </div>
  );
}
