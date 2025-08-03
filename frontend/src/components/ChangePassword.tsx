import React, { useState } from "react";
import { AUTH_API_BASE, fetchWithAuthRetry } from "../utils/api";

interface ChangePasswordProps {
  onClose: () => void;
}

export default function ChangePassword({ onClose }: ChangePasswordProps) {
  const [oldPassword, setOldPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  async function handleChange(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(null);
    if (newPassword !== confirm) {
      setError("Passwords do not match.");
      setLoading(false);
      return;
    }
    try {
      const res = await fetchWithAuthRetry(`${AUTH_API_BASE}/api/auth/v1/password/change`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ oldPassword, newPassword }),
      });
      if (res.ok) {
        setSuccess("Password changed successfully.");
      } else {
        setError("Password change failed.");
      }
    } catch {
      setError("Network error.");
    }
    setLoading(false);
  }

  return (
    <form className="flex flex-col gap-4" onSubmit={handleChange}>
      <h2 className="text-xl font-bold mb-2">Change Password</h2>
      {error && <div className="text-red-600 text-sm bg-red-100 rounded px-2 py-1">{error}</div>}
      {success && <div className="text-green-600 text-sm bg-green-100 rounded px-2 py-1">{success}</div>}
      <input
        type="password"
        placeholder="Current Password"
        className="border rounded px-3 py-2 bg-blue-50 dark:bg-gray-800 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-400"
        required
        value={oldPassword}
        onChange={e => setOldPassword(e.target.value)}
      />
      <input
        type="password"
        placeholder="New Password"
        className="border rounded px-3 py-2 bg-blue-50 dark:bg-gray-800 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-400"
        required
        value={newPassword}
        onChange={e => setNewPassword(e.target.value)}
      />
      <input
        type="password"
        placeholder="Confirm New Password"
        className="border rounded px-3 py-2 bg-blue-50 dark:bg-gray-800 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-400"
        required
        value={confirm}
        onChange={e => setConfirm(e.target.value)}
      />
      <button
        type="submit"
        className="bg-purple-600 text-white rounded px-4 py-2 font-semibold hover:bg-purple-700 shadow"
        disabled={loading}
      >
        {loading ? "Changing..." : "Change Password"}
      </button>
      <button className="bg-gray-600 text-white rounded px-4 py-2 font-semibold" onClick={onClose} type="button">
        Close
      </button>
    </form>
  );
}
