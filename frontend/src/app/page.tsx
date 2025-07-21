"use client";

import React from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";
import { useState } from "react";

const API_BASE = "http://localhost:12002/v1";

export default function Home() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [view, setView] = useState<"login" | "register" | "forgot" | "reset">("login");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // Form states
  const [loginEmail, setLoginEmail] = useState("");
  const [loginPassword, setLoginPassword] = useState("");
  const [registerEmail, setRegisterEmail] = useState("");
  const [registerUserName, setRegisterUserName] = useState("");
  const [registerPassword, setRegisterPassword] = useState("");
  const [registerConfirm, setRegisterConfirm] = useState("");
  const [forgotEmail, setForgotEmail] = useState("");
  const [resetToken, setResetToken] = useState("");
  const [resetPassword, setResetPassword] = useState("");
  const [resetConfirm, setResetConfirm] = useState("");

  // Helper for fetch with credentials
  async function apiFetch(url: string, options: RequestInit) {
    return fetch(url, { ...options, credentials: "include" });
  }

  async function handleLogin(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(null);
    try {
      const res = await apiFetch(`${API_BASE}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: loginEmail, password: loginPassword }),
      });
      if (res.ok) {
        setSuccess("Login successful!");
        setError(null);
        router.push("/weather");
      } else {
        setError("Login failed. Check your credentials.");
      }
    } catch {
      setError("Network error.");
    }
    setLoading(false);
  }

  async function handleRegister(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(null);
    if (registerPassword !== registerConfirm) {
      setError("Passwords do not match.");
      setLoading(false);
      return;
    }
    try {
      const res = await apiFetch(`${API_BASE}/auth/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          email: registerEmail,
          userName: registerUserName,
          password: registerPassword,
        }),
      });
      if (res.ok) {
        setSuccess("Registration successful! You can now log in.");
        setView("login");
      } else {
        setError("Registration failed. Try again.");
      }
    } catch {
      setError("Network error.");
    }
    setLoading(false);
  }

  async function handleForgot(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(null);
    try {
      const res = await apiFetch(`${API_BASE}/auth/password/forgot`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: forgotEmail }),
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

  // Handle direct navigation to reset page with token in URL
  React.useEffect(() => {
    const token = searchParams.get("token");
    if (token) {
      setResetToken(token);
      setView("reset");
    }
  }, [searchParams]);

  async function handleReset(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(null);
    if (resetPassword !== resetConfirm) {
      setError("Passwords do not match.");
      setLoading(false);
      return;
    }
    try {
      const res = await apiFetch(`${API_BASE}/auth/password/reset`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ resetToken, newPassword: resetPassword }),
      });
      if (res.ok) {
        setSuccess("Password reset successful! You can now log in.");
        setView("login");
      } else {
        setError("Password reset failed. Try again.");
      }
    } catch {
      setError("Network error.");
    }
    setLoading(false);
  }

  return (
    <div className="font-sans min-h-screen flex flex-col items-center justify-center p-8 bg-gradient-to-br from-blue-100 via-white to-blue-300 dark:from-gray-900 dark:via-gray-800 dark:to-gray-700">
      <main className="w-full max-w-md bg-white/80 dark:bg-gray-900/80 rounded-lg shadow-2xl p-8 flex flex-col gap-8 border border-blue-200 dark:border-gray-800 backdrop-blur">
        <div className="flex justify-between items-center mb-4">
          <h1 className="text-2xl font-bold text-blue-700 dark:text-blue-200">Authentication Demo</h1>
        </div>
        {error && <div className="text-red-600 text-sm bg-red-100 dark:bg-red-900 rounded px-2 py-1">{error}</div>}
        {success && <div className="text-green-600 text-sm bg-green-100 dark:bg-green-900 rounded px-2 py-1">{success}</div>}
        {view === "login" && (
          <form className="flex flex-col gap-4" onSubmit={handleLogin}>
            <input
              type="email"
              placeholder="Email"
              className="border rounded px-3 py-2 bg-blue-50 dark:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-400"
              required
              value={loginEmail}
              onChange={e => setLoginEmail(e.target.value)}
            />
            <input
              type="password"
              placeholder="Password"
              className="border rounded px-3 py-2 bg-blue-50 dark:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-400"
              required
              value={loginPassword}
              onChange={e => setLoginPassword(e.target.value)}
            />
            <button
              type="submit"
              className="bg-blue-600 text-white rounded px-4 py-2 font-semibold hover:bg-blue-700 shadow"
              disabled={loading}
            >
              {loading ? "Logging in..." : "Login"}
            </button>
            <div className="flex justify-between text-sm mt-2">
              <button
                type="button"
                className="text-blue-600 hover:underline"
                onClick={() => setView("register")}
              >
                Register
              </button>
              <button
                type="button"
                className="text-blue-600 hover:underline"
                onClick={() => setView("forgot")}
              >
                Forgot Password?
              </button>
            </div>
          </form>
        )}
        {view === "register" && (
          <form className="flex flex-col gap-4" onSubmit={handleRegister}>
            <input
              type="email"
              placeholder="Email"
              className="border rounded px-3 py-2 bg-blue-50 dark:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-400"
              required
              value={registerEmail}
              onChange={e => setRegisterEmail(e.target.value)}
            />
            <input
              type="text"
              placeholder="Username"
              className="border rounded px-3 py-2 bg-blue-50 dark:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-400"
              required
              value={registerUserName}
              onChange={e => setRegisterUserName(e.target.value)}
            />
            <input
              type="password"
              placeholder="Password"
              className="border rounded px-3 py-2 bg-blue-50 dark:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-400"
              required
              value={registerPassword}
              onChange={e => setRegisterPassword(e.target.value)}
            />
            <input
              type="password"
              placeholder="Confirm Password"
              className="border rounded px-3 py-2 bg-blue-50 dark:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-400"
              required
              value={registerConfirm}
              onChange={e => setRegisterConfirm(e.target.value)}
            />
            <button
              type="submit"
              className="bg-green-600 text-white rounded px-4 py-2 font-semibold hover:bg-green-700 shadow"
              disabled={loading}
            >
              {loading ? "Registering..." : "Register"}
            </button>
            <div className="flex justify-between text-sm mt-2">
              <button
                type="button"
                className="text-blue-600 hover:underline"
                onClick={() => setView("login")}
              >
                Back to Login
              </button>
            </div>
          </form>
        )}
        {view === "forgot" && (
          <form className="flex flex-col gap-4" onSubmit={handleForgot}>
            <input
              type="email"
              placeholder="Email"
              className="border rounded px-3 py-2 bg-blue-50 dark:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-400"
              required
              value={forgotEmail}
              onChange={e => setForgotEmail(e.target.value)}
            />
            <button
              type="submit"
              className="bg-yellow-600 text-white rounded px-4 py-2 font-semibold hover:bg-yellow-700 shadow"
              disabled={loading}
            >
              {loading ? "Sending..." : "Send Reset Link"}
            </button>
            <div className="flex justify-between text-sm mt-2">
              <button
                type="button"
                className="text-blue-600 hover:underline"
                onClick={() => setView("login")}
              >
                Back to Login
              </button>
              <button
                type="button"
                className="text-blue-600 hover:underline"
                onClick={() => setView("reset")}
              >
                Enter Reset Token
              </button>
            </div>
          </form>
        )}
        {view === "reset" && (
          <form className="flex flex-col gap-4" onSubmit={handleReset}>
            <input
              type="text"
              placeholder="Reset Token"
              className="border rounded px-3 py-2 bg-blue-50 dark:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-400"
              required
              value={resetToken}
              onChange={e => setResetToken(e.target.value)}
            />
            <input
              type="password"
              placeholder="New Password"
              className="border rounded px-3 py-2 bg-blue-50 dark:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-400"
              required
              value={resetPassword}
              onChange={e => setResetPassword(e.target.value)}
            />
            <input
              type="password"
              placeholder="Confirm New Password"
              className="border rounded px-3 py-2 bg-blue-50 dark:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-blue-400"
              required
              value={resetConfirm}
              onChange={e => setResetConfirm(e.target.value)}
            />
            <button
              type="submit"
              className="bg-yellow-600 text-white rounded px-4 py-2 font-semibold hover:bg-yellow-700 shadow"
              disabled={loading}
            >
              {loading ? "Resetting..." : "Reset Password"}
            </button>
            <div className="flex justify-between text-sm mt-2">
              <button
                type="button"
                className="text-blue-600 hover:underline"
                onClick={() => setView("login")}
              >
                Back to Login
              </button>
            </div>
          </form>
        )}
      </main>
    </div>
  );
}
