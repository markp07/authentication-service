import React, { useEffect, useState } from "react";
import { AUTH_API_BASE, fetchWithAuthRetry } from "../utils/api";
import { User } from "../types/User";
import { IconUser, IconMail, IconEdit, IconCheck, IconX, IconTrash, IconCalendar } from "@tabler/icons-react";

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

  const formatDate = (dateString?: string) => {
    if (!dateString) return "N/A";
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' });
  };

  return (
    <div className="p-6 space-y-6 max-w-4xl">
      <h1 className="text-3xl font-bold text-gray-900 dark:text-white">Profile</h1>

      {/* Profile Card */}
      <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4 flex items-center gap-2">
          <IconUser size={20} />
          Profile
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
          <div className="space-y-4">
            <div className="flex items-center justify-between py-3 border-b border-gray-200 dark:border-gray-700">
              <div className="flex items-center gap-2">
                <IconUser size={18} className="text-gray-500 dark:text-gray-400" />
                <span className="text-gray-600 dark:text-gray-400">Username</span>
              </div>
              <div className="flex items-center gap-3">
                <span className="text-gray-900 dark:text-white font-medium">{user?.userName}</span>
                <button
                  onClick={() => setEditing(true)}
                  className="flex items-center gap-1 px-3 py-1 text-sm bg-blue-50 dark:bg-blue-900/20 hover:bg-blue-100 dark:hover:bg-blue-900/30 text-blue-600 dark:text-blue-400 rounded-lg font-medium transition-colors"
                >
                  <IconEdit size={16} />
                  Edit
                </button>
              </div>
            </div>
            <div className="flex items-center justify-between py-3 border-b border-gray-200 dark:border-gray-700">
              <div className="flex items-center gap-2">
                <IconMail size={18} className="text-gray-500 dark:text-gray-400" />
                <span className="text-gray-600 dark:text-gray-400">Email</span>
              </div>
              <span className="text-gray-900 dark:text-white font-medium">{user?.email}</span>
            </div>
            <div className="flex items-center justify-between py-3">
              <div className="flex items-center gap-2">
                <IconCalendar size={18} className="text-gray-500 dark:text-gray-400" />
                <span className="text-gray-600 dark:text-gray-400">Created</span>
              </div>
              <span className="text-gray-900 dark:text-white font-medium">{formatDate(user?.createdAt)}</span>
            </div>
          </div>
        )}
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
          <div className="flex items-center justify-between py-2 border-b border-gray-200 dark:border-gray-700">
            <span className="text-gray-600 dark:text-gray-400">Passkey</span>
            <span className={`px-3 py-1 rounded-full text-sm font-medium ${
              user?.passkeyEnabled
                ? "bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400"
                : "bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300"
            }`}>
              {user?.passkeyEnabled ? "Enabled" : "Disabled"}
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

      {/* Delete Account Section */}
      <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6">
        <h3 className="text-lg font-semibold text-red-600 dark:text-red-400 mb-2">Danger Zone</h3>
        <p className="text-sm text-gray-600 dark:text-gray-400 mb-4">
          Once you delete your account, there is no going back. Please be certain.
        </p>
        <button
          onClick={onDeleteAccount}
          className="flex items-center gap-2 px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg font-medium transition-colors"
        >
          <IconTrash size={18} />
          Delete Account
        </button>
      </div>
    </div>
  );
}
