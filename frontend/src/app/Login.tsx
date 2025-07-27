import React, { useState } from "react";

interface LoginProps {
  onSuccess: () => void;
  onRegister: () => void;
  onForgot: () => void;
}

const API_BASE = "http://localhost:12002/v1";

export default function Login({ onSuccess, onRegister, onForgot }: LoginProps) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function apiFetch(url: string, options: RequestInit) {
    return fetch(url, { ...options, credentials: "include" });
  }

  async function handleLogin(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const res = await apiFetch(`${API_BASE}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });
      if (res.ok) {
        onSuccess();
      } else {
        setError("Login failed. Check your credentials.");
      }
    } catch {
      setError("Network error.");
    }
    setLoading(false);
  }

  return (
    <form className="flex flex-col gap-4" onSubmit={handleLogin}>
      <h2 className="text-xl font-bold mb-2">Login</h2>
      {error && <div className="text-red-600 text-sm bg-red-100 rounded px-2 py-1">{error}</div>}
      <input
        type="email"
        placeholder="Email"
        className="border rounded px-3 py-2 bg-blue-50 focus:outline-none focus:ring-2 focus:ring-blue-400"
        required
        value={email}
        onChange={e => setEmail(e.target.value)}
      />
      <input
        type="password"
        placeholder="Password"
        className="border rounded px-3 py-2 bg-blue-50 focus:outline-none focus:ring-2 focus:ring-blue-400"
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
    </form>
  );
}

