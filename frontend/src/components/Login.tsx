import React, { useState } from "react";

interface LoginProps {
  onSuccess: () => void;
  onRegister: () => void;
  onForgot: () => void;
}

const isDev = typeof window !== "undefined" && window.location.hostname === "localhost";
const AUTH_API_BASE = isDev
  ? (process.env.NEXT_PUBLIC_API_URL || "http://localhost:12002/v1")
  : "https://demo.markpost.dev";

export default function Login({ onSuccess, onRegister, onForgot }: LoginProps) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [step, setStep] = useState<'login' | '2fa'>('login');
  const [totpCode, setTotpCode] = useState('');

  async function apiFetch(url: string, options: RequestInit) {
    return fetch(url, { ...options, credentials: "include" });
  }

  async function handleLogin(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const res = await apiFetch(`${AUTH_API_BASE}/api/auth/v1/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });
      const data = await res.json();
      if (res.status === 200 && data.code === "LOGIN_SUCCESS") {
        onSuccess();
      } else if (res.status === 202 && data.code === "2FA_REQUIRED") {
        setStep('2fa');
      } else {
        setError("Login failed. Check your credentials.");
      }
    } catch {
      setError("Network error.");
    }
    setLoading(false);
  }

  async function handle2fa(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const res = await apiFetch(`${AUTH_API_BASE}/api/auth/v1/2fa/verify`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ code: totpCode }),
      });
      const data = await res.json();
      if (res.status === 200 && data.code === "LOGIN_SUCCESS") {
        onSuccess();
      } else {
        setError("Invalid TOTP code. Try again.");
      }
    } catch {
      setError("Network error.");
    }
    setLoading(false);
  }

  return (
    <form className="flex flex-col gap-4" onSubmit={step === 'login' ? handleLogin : handle2fa}>
      <h2 className="text-xl font-bold mb-2">Login</h2>
      {error && <div className="text-red-600 text-sm bg-red-100 rounded px-2 py-1">{error}</div>}
      {step === 'login' && (
        <>
          <input
            type="email"
            placeholder="Email"
            className="border rounded px-3 py-2 bg-blue-50 dark:bg-gray-800 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-400"
            required
            value={email}
            onChange={e => setEmail(e.target.value)}
          />
          <input
            type="password"
            placeholder="Password"
            className="border rounded px-3 py-2 bg-blue-50 dark:bg-gray-800 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-400"
            required
            value={password}
            onChange={e => setPassword(e.target.value)}
          />
          <button
            type="submit"
            className="bg-blue-600 text-white rounded px-4 py-2 font-semibold hover:bg-blue-700 shadow"
            disabled={loading}
          >
            {loading ? "Logging in..." : "Login"}
          </button>
          <div className="flex justify-between text-sm mt-2">
            <button type="button" className="text-blue-600 hover:underline" onClick={onRegister}>
              Register
            </button>
            <button type="button" className="text-blue-600 hover:underline" onClick={onForgot}>
              Forgot Password?
            </button>
          </div>
        </>
      )}
      {step === '2fa' && (
        <>
          <label htmlFor="totp-code" className="font-semibold">Enter your 2FA code:</label>
          <input
            id="totp-code"
            type="text"
            inputMode="numeric"
            pattern="\d{6}"
            maxLength={6}
            required
            className="border rounded px-3 py-2 bg-blue-50 dark:bg-gray-800 dark:text-white text-center text-lg tracking-widest"
            value={totpCode}
            onChange={e => setTotpCode(e.target.value)}
          />
          <button
            type="submit"
            className="bg-blue-600 text-white rounded px-4 py-2 font-semibold hover:bg-blue-700 shadow"
            disabled={loading}
          >
            {loading ? "Verifying..." : "Verify"}
          </button>
        </>
      )}
    </form>
  );
}
