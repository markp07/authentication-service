import React, { useState } from "react";

interface ResetPasswordProps {
  onBack: () => void;
  onLogin: () => void;
}

const API_BASE = "http://localhost:12002/v1";

export default function ResetPassword({ onBack, onLogin }: ResetPasswordProps) {
  const [resetToken, setResetToken] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  async function apiFetch(url: string, options: RequestInit) {
    return fetch(url, { ...options, credentials: "include" });
  }

  async function handleReset(e: React.FormEvent) {
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
      const res = await apiFetch(`${API_BASE}/auth/password/reset`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ resetToken, newPassword }),
      });
      if (res.ok) {
        setSuccess("Password reset successful! You can now log in.");
        setTimeout(() => {
          onLogin();
        }, 1200);
      } else {
        setError("Password reset failed. Try again.");
      }
    } catch {
      setError("Network error.");
    }
    setLoading(false);
  }

  return (
    <form className="flex flex-col gap-4" onSubmit={handleReset}>
      <h2 className="text-xl font-bold mb-2">Reset Password</h2>
      {error && <div className="text-red-600 text-sm bg-red-100 rounded px-2 py-1">{error}</div>}
      {success && <div className="text-green-600 text-sm bg-green-100 rounded px-2 py-1">{success}</div>}
      <input
        type="text"
        placeholder="Reset Token"
        className="border rounded px-3 py-2 bg-blue-50 focus:outline-none focus:ring-2 focus:ring-blue-400"
        required
        value={resetToken}
        onChange={e => setResetToken(e.target.value)}
      />
      <input
        type="password"
        placeholder="New Password"
        className="border rounded px-3 py-2 bg-blue-50 focus:outline-none focus:ring-2 focus:ring-blue-400"
        required
        value={newPassword}
        onChange={e => setNewPassword(e.target.value)}
      />
      <input
        type="password"
        placeholder="Confirm Password"
        className="border rounded px-3 py-2 bg-blue-50 focus:outline-none focus:ring-2 focus:ring-blue-400"
        required
        value={confirm}
        onChange={e => setConfirm(e.target.value)}
      />
      <button
        type="submit"
        className="bg-purple-600 text-white rounded px-4 py-2 font-semibold hover:bg-purple-700 shadow"
        disabled={loading}
      >
        {loading ? "Resetting..." : "Reset Password"}
      </button>
      <div className="flex justify-between text-sm mt-2">
        <button type="button" className="text-blue-600 hover:underline" onClick={onBack}>
          Back
        </button>
        <button type="button" className="text-blue-600 hover:underline" onClick={onLogin}>
          Back to Login
        </button>
      </div>
    </form>
  );
}

