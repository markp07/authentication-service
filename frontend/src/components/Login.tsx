import React, { useState } from "react";

interface LoginProps {
  onSuccess: () => void;
  onRegister: () => void;
  onForgot: () => void;
}

const isDev = typeof window !== "undefined" && window.location.hostname === "localhost";
const AUTH_API_BASE = isDev
  ? (process.env.NEXT_PUBLIC_API_URL || "http://localhost:12002")
  : "https://demo.markpost.dev";

export default function Login({ onSuccess, onRegister, onForgot }: LoginProps) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [step, setStep] = useState<'login' | '2fa'>('login');
  const [totpCode, setTotpCode] = useState('');
  const [passkeyLoading, setPasskeyLoading] = useState(false);
  const [passkeyError, setPasskeyError] = useState<string | null>(null);

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

  async function handlePasskeyLogin() {
    setPasskeyLoading(true);
    setPasskeyError(null);
    try {
      // 1. Get email from input
      if (!email) {
        setPasskeyError("Please enter your email.");
        setPasskeyLoading(false);
        return;
      }
      // 2. Get assertion options from backend
      const res = await apiFetch(`${AUTH_API_BASE}/v1/passkey/login/start?email=${encodeURIComponent(email)}`, { method: "POST" });
      if (!res.ok) throw new Error("Failed to start passkey login");
      const options = await res.json();
      options.challenge = Uint8Array.from(atob(options.challenge), c => c.charCodeAt(0));
      if (options.allowCredentials) {
        options.allowCredentials = options.allowCredentials.map((cred: { id: string } & Record<string, unknown>) => ({ ...cred, id: Uint8Array.from(atob(cred.id), c => c.charCodeAt(0)) }));
      }
      // 3. Call WebAuthn API
      const assertion = await navigator.credentials.get({ publicKey: options });
      // 4. Send assertion to backend
      const finishRes = await apiFetch(`${AUTH_API_BASE}/v1/passkey/login/finish?email=${encodeURIComponent(email)}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(assertion),
      });
      const finishData = await finishRes.json();
      if (finishRes.status === 200 && finishData.code === "LOGIN_SUCCESS") {
        onSuccess();
      } else {
        setPasskeyError("Passkey login failed.");
      }
    } catch (err) {
      setPasskeyError("Passkey login failed.");
    }
    setPasskeyLoading(false);
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
          <button
            type="button"
            className="bg-green-600 text-white rounded px-4 py-2 font-semibold hover:bg-green-700 shadow mt-2"
            onClick={handlePasskeyLogin}
            disabled={passkeyLoading}
          >
            {passkeyLoading ? "Logging in with Passkey..." : "Login with Passkey"}
          </button>
          {passkeyError && <div className="text-red-600 text-xs mt-1">{passkeyError}</div>}
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
            autoComplete="one-time-code"
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
          <div className="flex justify-between text-sm mt-2">
            <button type="button" className="text-blue-600 hover:underline" onClick={() => setStep('login')}>
              ← Back to Login
            </button>
            <button type="button" className="text-blue-600 hover:underline" onClick={() => window.open('mailto:support@markpost.dev?subject=Lost 2FA', '_blank')}>Lost 2FA?</button>
          </div>
        </>
      )}
    </form>
  );
}
