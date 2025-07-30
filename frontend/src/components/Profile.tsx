import React, { useEffect, useState } from "react";
import { User } from "../types/User";

interface ProfileProps {
  onClose: () => void;
}

const isDev = typeof window !== "undefined" && window.location.hostname === "localhost";
const AUTH_API_BASE = isDev
  ? (process.env.NEXT_PUBLIC_API_URL || "http://localhost:12002")
  : "https://demo.markpost.dev";

export default function Profile({ onClose }: ProfileProps) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [editing, setEditing] = useState(false);
  const [userName, setuserName] = useState("");
  const [userNameError, setuserNameError] = useState<string | null>(null);
  const [userNameSuccess, setuserNameSuccess] = useState<string | null>(null);

  useEffect(() => {
    async function fetchUser() {
      setLoading(true);
      setError(null);
      try {
        const res = await fetch(`${AUTH_API_BASE}/api/auth/v1/user`, { credentials: "include" });
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
    if (user) setuserName(user.userName || "");
  }, [user]);

  async function handleuserNameSave() {
    setuserNameError(null);
    setuserNameSuccess(null);
    if (!userName.trim()) {
      setuserNameError("userName cannot be empty.");
      return;
    }
    try {
      const res = await fetch(`${AUTH_API_BASE}/user`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ userName }),
      });
      if (res.ok) {
        setuserNameSuccess("userName updated successfully.");
        setEditing(false);
        if (user) {
          setUser({ ...user, userName } as User);
        }
      } else {
        setuserNameError("Failed to update userName.");
      }
    } catch {
      setuserNameError("Network error.");
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
            <span className="font-semibold">userName:</span>
            {editing ? (
              <>
                <input
                  className="border rounded px-2 py-1 text-sm bg-white dark:bg-gray-900"
                  value={userName}
                  onChange={e => setuserName(e.target.value)}
                  autoFocus
                />
                <button className="bg-green-600 text-white rounded px-2 py-1 text-xs font-semibold" onClick={handleuserNameSave} type="button">Save</button>
                <button className="bg-gray-400 text-white rounded px-2 py-1 text-xs font-semibold" onClick={() => { setEditing(false); setuserName(user.userName); }} type="button">Cancel</button>
              </>
            ) : (
              <>
                <span>{user.userName}</span>
                <button className="ml-2 text-xs text-blue-600 underline" onClick={() => setEditing(true)} type="button">Edit</button>
              </>
            )}
          </div>
          {userNameError && <div className="text-red-600 text-xs mt-1">{userNameError}</div>}
          {userNameSuccess && <div className="text-green-600 text-xs mt-1">{userNameSuccess}</div>}
          <div><span className="font-semibold">2FA Enabled:</span> {user.twoFAEnabled ? "Yes" : "No"}</div>
        </div>
      ) : null}
      <button className="bg-gray-600 text-white rounded px-4 py-2 font-semibold" onClick={onClose}>
        Close
      </button>
    </div>
  );
}
