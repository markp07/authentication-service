"use client";
import React, { useState } from "react";
import { IconUser, IconLock } from "@tabler/icons-react";

export default function ProfilePage() {
  const [userName, setUserName] = useState("");
  const [oldPassword, setOldPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [message, setMessage] = useState<string | null>(null);

  const handlePasswordChange = async (e: React.FormEvent) => {
    e.preventDefault();
    if (newPassword !== confirmPassword) {
      setMessage("New passwords do not match.");
      return;
    }
    // TODO: Call password change API when available
    setMessage("Password change API not available yet.");
  };

  return (
    <div className="min-h-screen flex flex-col items-center justify-center p-8 bg-gradient-to-br from-blue-100 via-white to-blue-300 dark:from-gray-900 dark:via-gray-800 dark:to-gray-700">
      <div className="w-full max-w-md bg-white/80 dark:bg-gray-900/80 rounded-lg shadow-2xl p-8 border border-blue-200 dark:border-gray-800 backdrop-blur">
        <h1 className="text-3xl font-bold mb-6 text-blue-700 dark:text-blue-200 text-center">Profile</h1>
        <form className="mb-8">
          <label className="block mb-2 font-semibold text-blue-700 dark:text-blue-200 flex items-center gap-2">
            <IconUser size={20} /> User Name
          </label>
          <input
            type="text"
            className="w-full px-3 py-2 rounded border border-blue-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-black dark:text-white mb-4"
            value={userName}
            onChange={e => setUserName(e.target.value)}
            placeholder="Edit user name (not yet available)"
            disabled
          />
        </form>
        <form onSubmit={handlePasswordChange}>
          <label className="block mb-2 font-semibold text-blue-700 dark:text-blue-200 flex items-center gap-2">
            <IconLock size={20} /> Change Password
          </label>
          <input
            type="password"
            className="w-full px-3 py-2 rounded border border-blue-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-black dark:text-white mb-2"
            value={oldPassword}
            onChange={e => setOldPassword(e.target.value)}
            placeholder="Current password"
            required
          />
          <input
            type="password"
            className="w-full px-3 py-2 rounded border border-blue-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-black dark:text-white mb-2"
            value={newPassword}
            onChange={e => setNewPassword(e.target.value)}
            placeholder="New password"
            required
          />
          <input
            type="password"
            className="w-full px-3 py-2 rounded border border-blue-300 dark:border-gray-700 bg-white dark:bg-gray-800 text-black dark:text-white mb-4"
            value={confirmPassword}
            onChange={e => setConfirmPassword(e.target.value)}
            placeholder="Confirm new password"
            required
          />
          <button
            type="submit"
            className="w-full px-4 py-2 rounded bg-blue-600 text-white hover:bg-blue-700 transition font-semibold"
          >
            Change Password
          </button>
        </form>
        {message && <p className="mt-4 text-center text-red-600">{message}</p>}
      </div>
    </div>
  );
}

