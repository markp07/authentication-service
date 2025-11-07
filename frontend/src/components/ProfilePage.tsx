import React, { useEffect, useState } from "react";
import { AUTH_API_BASE, fetchWithAuthRetry } from "../utils/api";
import { User } from "../types/User";
import { IconUser, IconMail, IconEdit, IconCheck, IconX, IconTrash, IconShield } from "@tabler/icons-react";

interface ProfilePageProps {
  onSecurity: () => void;
  onDeleteAccount: () => void;
}

export default function ProfilePage({ onSecurity, onDeleteAccount }: ProfilePageProps) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [editing, setEditing] = useState(false);
  const [userName, setUserName] = useState("");
  const [userNameError, setUserNameError] = useState<string | null>(null);
  const [userNameSuccess, setUserNameSuccess] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    async function fetchUser() {
      setLoading(true);
      setError(null);
      try {
        const res = await fetchWithAuthRetry(`${AUTH_API_BASE}/api/auth/v1/user`);
        if (res.ok) {
          const userData = await res.json();
          setUser(userData);
          setUserName(userData.userName || "");
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

  async function handleUserNameSave() {
    setUserNameError(null);
    setUserNameSuccess(null);
    if (!userName.trim()) {
      setUserNameError("Username cannot be empty.");
      return;
    }
    setSaving(true);
    try {
      const res = await fetchWithAuthRetry(`${AUTH_API_BASE}/api/auth/v1/user`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ userName }),
      });
      if (res.ok) {
        setUserNameSuccess("Username updated successfully.");
        setEditing(false);
        if (user) {
          setUser({ ...user, userName } as User);
        }
      } else {
        setUserNameError("Failed to update username.");
      }
    } catch {
      setUserNameError("Network error.");
    }
    setSaving(false);
  }

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
      <h1 className="text-3xl font-bold text-gray-900 dark:text-white">Profile</h1>

      {/* User Information Card */}
      <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg overflow-hidden">
        <div className="bg-gradient-to-r from-blue-500 to-blue-600 h-24"></div>
        <div className="px-6 pb-6 -mt-12">
          <div className="flex items-end gap-4">
            <div className="w-24 h-24 bg-white dark:bg-gray-700 rounded-full flex items-center justify-center shadow-lg border-4 border-white dark:border-gray-800">
              <IconUser size={48} className="text-blue-600 dark:text-blue-400" />
            </div>
            <div className="flex-1 mb-2">
              <h2 className="text-2xl font-bold text-gray-900 dark:text-white">{user?.userName}</h2>
              <div className="flex items-center gap-2 text-gray-600 dark:text-gray-400 mt-1">
                <IconMail size={16} />
                <span>{user?.email}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Edit Username Card */}
      <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4 flex items-center gap-2">
          <IconEdit size={20} />
          Username Settings
        </h3>
        {editing ? (
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Username
              </label>
              <input
                type="text"
                value={userName}
                onChange={(e) => setUserName(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
                placeholder="Enter new username"
              />
            </div>
            {userNameError && (
              <div className="text-red-600 dark:text-red-400 text-sm">{userNameError}</div>
            )}
            {userNameSuccess && (
              <div className="text-green-600 dark:text-green-400 text-sm">{userNameSuccess}</div>
            )}
            <div className="flex gap-2">
              <button
                onClick={handleUserNameSave}
                disabled={saving}
                className="flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium transition-colors disabled:opacity-50"
              >
                <IconCheck size={18} />
                {saving ? "Saving..." : "Save"}
              </button>
              <button
                onClick={() => {
                  setEditing(false);
                  setUserName(user?.userName || "");
                  setUserNameError(null);
                  setUserNameSuccess(null);
                }}
                disabled={saving}
                className="flex items-center gap-2 px-4 py-2 bg-gray-200 hover:bg-gray-300 dark:bg-gray-700 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300 rounded-lg font-medium transition-colors"
              >
                <IconX size={18} />
                Cancel
              </button>
            </div>
          </div>
        ) : (
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-600 dark:text-gray-400 text-sm">Current username</p>
              <p className="text-lg font-semibold text-gray-900 dark:text-white">{user?.userName}</p>
            </div>
            <button
              onClick={() => setEditing(true)}
              className="flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium transition-colors"
            >
              <IconEdit size={18} />
              Edit
            </button>
          </div>
        )}
      </div>

      {/* Quick Actions Card */}
      <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Quick Actions</h3>
        <div className="space-y-3">
          <button
            onClick={onSecurity}
            className="w-full flex items-center gap-3 px-4 py-3 bg-blue-50 dark:bg-blue-900/20 hover:bg-blue-100 dark:hover:bg-blue-900/30 text-blue-700 dark:text-blue-400 rounded-lg font-medium transition-colors"
          >
            <IconShield size={20} />
            <div className="flex-1 text-left">
              <div className="font-semibold">Security Settings</div>
              <div className="text-sm opacity-75">Manage 2FA, passkeys, and password</div>
            </div>
          </button>
          <button
            onClick={onDeleteAccount}
            className="w-full flex items-center gap-3 px-4 py-3 bg-red-50 dark:bg-red-900/20 hover:bg-red-100 dark:hover:bg-red-900/30 text-red-700 dark:text-red-400 rounded-lg font-medium transition-colors"
          >
            <IconTrash size={20} />
            <div className="flex-1 text-left">
              <div className="font-semibold">Delete Account</div>
              <div className="text-sm opacity-75">Permanently delete your account</div>
            </div>
          </button>
        </div>
      </div>

      {/* Account Status Card */}
      <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">Account Status</h3>
        <div className="space-y-3">
          <div className="flex items-center justify-between py-2 border-b border-gray-200 dark:border-gray-700">
            <span className="text-gray-600 dark:text-gray-400">Two-Factor Authentication</span>
            <span className={`px-3 py-1 rounded-full text-sm font-medium ${
              user?.twoFactorEnabled
                ? "bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400"
                : "bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300"
            }`}>
              {user?.twoFactorEnabled ? "Enabled" : "Disabled"}
            </span>
          </div>
          <div className="flex items-center justify-between py-2">
            <span className="text-gray-600 dark:text-gray-400">Email</span>
            <span className="px-3 py-1 rounded-full text-sm font-medium bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400">
              Verified
            </span>
          </div>
        </div>
      </div>
    </div>
  );
}
