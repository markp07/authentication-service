import React, { useState } from "react";

interface ForgotPasswordProps {
  onBack: () => void;
  onReset: () => void;
}

const isDev = typeof window !== "undefined" && window.location.hostname === "localhost";
const AUTH_API_BASE = isDev
  ? (process.env.NEXT_PUBLIC_API_URL || "http://localhost:12002/v1")
  : "https://demo.markpost.dev";

export default function ForgotPassword({ onBack, onReset }: ForgotPasswordProps) {
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  async function apiFetch(url: string, options: RequestInit) {
    return fetch(url, { ...options, credentials: "include" });
  }

  async function handleForgot(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(null);
    try {
      const res = await apiFetch(`${AUTH_API_BASE}/api/auth/v1/password/forgot`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email }),
      });
      if (res.ok) {
        setSuccess("Password reset link sent (if email exists).");
      } else {
        setError("Could not send reset link.");
      }
    } catch {
      setError("Network error.");
    }
    setLoading(false);
  }

  return (
    <form className="flex flex-col gap-4" onSubmit={handleForgot}>
      <h2 className="text-xl font-bold mb-2">Forgot Password</h2>
      {error && <div className="text-red-600 text-sm bg-red-100 rounded px-2 py-1">{error}</div>}
      {success && <div className="text-green-600 text-sm bg-green-100 rounded px-2 py-1">{success}</div>}
      <input
        type="email"
        placeholder="Email"
        className="border rounded px-3 py-2 bg-blue-50 focus:outline-none focus:ring-2 focus:ring-blue-400"
        required
        value={email}
        onChange={e => setEmail(e.target.value)}
      />
      <button
        type="submit"
        className="bg-yellow-600 text-white rounded px-4 py-2 font-semibold hover:bg-yellow-700 shadow"
        disabled={loading}
      >
        {loading ? "Sending..." : "Send Reset Link"}
      </button>
      <div className="flex justify-between text-sm mt-2">
        <button type="button" className="text-blue-600 hover:underline" onClick={onBack}>
          Back to Login
        </button>
        <button type="button" className="text-blue-600 hover:underline" onClick={onReset}>
          Enter Reset Token
        </button>
      </div>
    </form>
  );
}

