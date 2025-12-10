'use client';

import React, { useState } from "react";
import { useTranslations } from 'next-intl';

interface RegisterProps {
  onSuccess: () => void;
  onLogin: () => void;
}

const isDev = typeof window !== "undefined" && window.location.hostname === "localhost";
const AUTH_API_BASE = isDev
  ? (process.env.NEXT_PUBLIC_API_URL || "http://localhost:12002")
  : "https://demo.markpost.dev";

export default function Register({ onSuccess, onLogin }: RegisterProps) {
  const t = useTranslations('register');
  const tCommon = useTranslations('common');
  const [email, setEmail] = useState("");
  const [userName, setUserName] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  async function apiFetch(url: string, options: RequestInit) {
    return fetch(url, { ...options, credentials: "include" });
  }

  async function handleRegister(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(null);
    if (password !== confirm) {
      setError(t('passwordMismatch'));
      setLoading(false);
      return;
    }
    try {
      const res = await apiFetch(`${AUTH_API_BASE}/api/auth/v1/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, userName, password }),
      });
      if (res.ok) {
        setSuccess(t('registerSuccess'));
        setTimeout(() => {
          onSuccess();
        }, 1200);
      } else {
        setError(t('registerError'));
      }
    } catch {
      setError(t('networkError'));
    }
    setLoading(false);
  }

  return (
    <form className="flex flex-col gap-4" onSubmit={handleRegister}>
      <h2 className="text-xl font-bold mb-2">{t('title')}</h2>
      {error && <div className="text-red-600 text-sm bg-red-100 rounded px-2 py-1">{error}</div>}
      {success && <div className="text-green-600 text-sm bg-green-100 rounded px-2 py-1">{success}</div>}
      <input
        type="email"
        placeholder={t('email')}
        className="border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400"
        required
        value={email}
        onChange={e => setEmail(e.target.value)}
      />
      <input
        type="text"
        placeholder={t('username')}
        className="border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400"
        required
        value={userName}
        onChange={e => setUserName(e.target.value)}
      />
      <input
        type="password"
        placeholder={t('password')}
        className="border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400"
        required
        value={password}
        onChange={e => setPassword(e.target.value)}
      />
      <input
        type="password"
        placeholder={t('confirmPassword')}
        className="border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-400"
        required
        value={confirm}
        onChange={e => setConfirm(e.target.value)}
      />
      <button
        type="submit"
        className="bg-green-600 text-white rounded px-4 py-2 font-semibold hover:bg-green-700 shadow"
        disabled={loading}
      >
        {loading ? tCommon('loading') : t('registerButton')}
      </button>
      <button
        type="button"
        className="text-blue-600 hover:underline text-sm mt-2"
        onClick={onLogin}
      >
        ← {t('login')}
      </button>
    </form>
  );
}
