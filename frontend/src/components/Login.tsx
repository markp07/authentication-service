'use client';

import React, { useState } from "react";
import { useTranslations } from 'next-intl';
import { AUTH_API_BASE } from '@/utils/api';

interface LoginProps {
  onSuccess: () => void;
  onRegister: () => void;
  onForgot: () => void;
}


export default function Login({ onSuccess, onRegister, onForgot }: LoginProps) {
  const t = useTranslations('login');
  const tCommon = useTranslations('common');
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [step, setStep] = useState<'start' | 'email' | 'password' | '2fa'>('start');
  const [totpCode, setTotpCode] = useState(['', '', '', '', '', '']);
  const [passkeyLoading, setPasskeyLoading] = useState(false);
  const [passkeyError, setPasskeyError] = useState<string | null>(null);

  async function apiFetch(url: string, options: RequestInit) {
    return fetch(url, { ...options, credentials: "include" });
  }

  function resetToStart() {
    setStep('start');
    setEmail('');
    setPassword('');
    setError(null);
    setPasskeyError(null);
    setTotpCode(['', '', '', '', '', '']);
  }

  async function handlePasswordLogin(e: React.FormEvent) {
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
        setError(t('loginError'));
      }
    } catch {
      setError(t('networkError'));
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
        body: JSON.stringify({ code: totpCode.join('') }),
      });
      const data = await res.json();
      if (res.status === 200 && data.code === "LOGIN_SUCCESS") {
        onSuccess();
      } else {
        setError(t('loginError'));
      }
    } catch {
      setError(t('networkError'));
    }
    setLoading(false);
  }

  function base64urlToBase64(base64url: string): string {
    let base64 = base64url.replace(/-/g, '+').replace(/_/g, '/');
    while (base64.length % 4) base64 += '=';
    return base64;
  }

  function sanitizeExtensions(extensions: Record<string, unknown>): Record<string, unknown> {
    if (!extensions || typeof extensions !== "object") return extensions;
    const sanitized: Record<string, unknown> = {};
    for (const key in extensions) {
      const value = extensions[key];
      if (value !== null && value !== undefined && value !== "") {
        sanitized[key] = value;
      }
    }
    return sanitized;
  }

  function handleTotpChange(index: number, value: string) {
    // Only allow single digit
    if (value.length > 1) value = value.slice(-1);
    if (!/^\d*$/.test(value)) return;

    const newCode = [...totpCode];
    newCode[index] = value;
    setTotpCode(newCode);

    // Auto-focus next input
    if (value && index < 5) {
      const nextInput = document.getElementById(`totp-${index + 1}`);
      nextInput?.focus();
    }
  }

  function handleTotpKeyDown(index: number, e: React.KeyboardEvent) {
    // Handle backspace to move to previous input
    if (e.key === 'Backspace' && !totpCode[index] && index > 0) {
      const prevInput = document.getElementById(`totp-${index - 1}`);
      prevInput?.focus();
    }
  }

  async function handleEmailPasskeyLogin() {
    setPasskeyLoading(true);
    setPasskeyError(null);
    try {
      // Get assertion options from backend with email
      const res = await apiFetch(`${AUTH_API_BASE}/api/auth/v1/passkey/login/start`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email })
      });
      if (!res.ok) throw new Error("Failed to start passkey login");
      const options = await res.json();
      options.challenge = Uint8Array.from(atob(base64urlToBase64(options.challenge)), c => c.charCodeAt(0));
      if (options.allowCredentials) {
        options.allowCredentials = options.allowCredentials.map((cred: { id: string; transports?: string[]; type?: string }) => {
          const decodedId = Uint8Array.from(atob(base64urlToBase64(cred.id)), c => c.charCodeAt(0));
          return {
            type: cred.type || "public-key",
            id: decodedId,
            transports: Array.isArray(cred.transports) ? cred.transports : undefined,
          };
        });
      }
      if (options.extensions) {
        options.extensions = sanitizeExtensions(options.extensions);
      }

      // Call WebAuthn API
      const assertion = await navigator.credentials.get({ publicKey: options });

      // Send assertion to backend
      const finishRes = await apiFetch(`${AUTH_API_BASE}/api/auth/v1/passkey/login/finish`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          email,
          credential: assertion
        }),
      });
      const finishData = await finishRes.json();
      if (finishRes.status === 200 && finishData.code === "LOGIN_SUCCESS") {
        onSuccess();
      } else {
        setPasskeyError("Passkey login failed.");
      }
    } catch (err) {
      console.error(err);
      setPasskeyError("Passkey login failed.");
    }
    setPasskeyLoading(false);
  }

  async function handleUsernamelessPasskeyLogin() {
    setPasskeyLoading(true);
    setPasskeyError(null);
    try {
      // Get assertion options without email
      const res = await apiFetch(`${AUTH_API_BASE}/api/auth/v1/passkey/login/usernameless/start`, {
        method: "POST",
        headers: { "Content-Type": "application/json" }
      });
      if (!res.ok) throw new Error("Failed to start usernameless passkey login");

      const options = await res.json();

      // Convert challenge from base64url to Uint8Array
      options.challenge = Uint8Array.from(atob(base64urlToBase64(options.challenge)), c => c.charCodeAt(0));

      // Clean up extensions
      if (options.extensions) {
        options.extensions = sanitizeExtensions(options.extensions);
      }

      // Call WebAuthn API - browser will show all available passkeys for this RP
      const assertion = await navigator.credentials.get({ publicKey: options });

      if (!assertion) {
        throw new Error("No passkey selected");
      }

      // Send assertion to backend (no email needed)
      const finishRes = await apiFetch(`${AUTH_API_BASE}/api/auth/v1/passkey/login/usernameless/finish`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(assertion)
      });

      const finishData = await finishRes.json();
      if (finishRes.status === 200 && finishData.code === "LOGIN_SUCCESS") {
        onSuccess();
      } else {
        setPasskeyError("Usernameless passkey login failed.");
      }
    } catch (err) {
      console.error("Usernameless passkey login error:", err);
      setPasskeyError("Usernameless passkey login failed. Make sure you have registered passkeys for this site.");
    }
    setPasskeyLoading(false);
  }

  return (
    <div className="flex flex-col gap-4">
      <h2 className="text-xl font-bold mb-2">{t('title')}</h2>
      {error && <div className="text-red-600 text-sm bg-red-100 rounded px-2 py-1">{error}</div>}
      {passkeyError && <div className="text-red-600 text-sm bg-red-100 rounded px-2 py-1">{passkeyError}</div>}

      {/* Start Screen - Choose login method */}
      {step === 'start' && (
        <div className="flex flex-col gap-4">
          <p className="text-gray-600 dark:text-gray-300 text-center">{t('chooseMethod')}</p>

          <button
            type="button"
            className="bg-purple-600 text-white rounded px-4 py-3 font-semibold hover:bg-purple-700 shadow flex items-center justify-center gap-2"
            onClick={handleUsernamelessPasskeyLogin}
            disabled={passkeyLoading}
          >
            {passkeyLoading ? tCommon('loading') : (
              <>
                <span>🔑</span>
                <span>{t('usePasskey')}</span>
              </>
            )}
          </button>

          <div className="flex items-center gap-4">
            <div className="flex-1 border-t border-gray-300"></div>
            <span className="text-gray-500 text-sm">{t('or')}</span>
            <div className="flex-1 border-t border-gray-300"></div>
          </div>

          <button
            type="button"
            className="bg-blue-600 text-white rounded px-4 py-3 font-semibold hover:bg-blue-700 shadow"
            onClick={() => setStep('email')}
          >
            {t('loginWithEmail')}
          </button>

          <div className="text-center mt-4">
            <span className="text-gray-600 dark:text-gray-300">{t('noAccount')} </span>
            <button
              type="button"
              className="text-blue-600 hover:underline"
              onClick={onRegister}
            >
              {t('register')}
            </button>
          </div>
        </div>
      )}

      {/* Email Entry Screen */}
      {step === 'email' && (
        <form className="flex flex-col gap-4" onSubmit={(e) => e.preventDefault()}>
          <div className="flex items-center gap-2">
            <button
              type="button"
              className="text-blue-600 hover:underline text-sm"
              onClick={resetToStart}
            >
              ← {tCommon('back')}
            </button>
          </div>

          <input
            type="email"
            placeholder={t('email')}
            className="border rounded px-3 py-2 bg-blue-50 dark:bg-gray-800 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-400"
            value={email}
            onChange={e => setEmail(e.target.value)}
            autoFocus
            required
          />

          <div className="flex flex-col gap-2">
            <button
              type="button"
              className="bg-green-600 text-white rounded px-4 py-2 font-semibold hover:bg-green-700 shadow disabled:opacity-50"
              onClick={handleEmailPasskeyLogin}
              disabled={!email || passkeyLoading}
            >
              {passkeyLoading ? tCommon('loading') : t('usePasskey')}
            </button>

            <button
              type="button"
              className="bg-blue-600 text-white rounded px-4 py-2 font-semibold hover:bg-blue-700 shadow disabled:opacity-50"
              onClick={() => email && setStep('password')}
              disabled={!email}
            >
              Next
            </button>
          </div>

          <div className="text-center">
            <button
              type="button"
              className="text-blue-600 hover:underline text-sm"
              onClick={onForgot}
            >
              {t('forgotPassword')}
            </button>
          </div>
        </form>
      )}

      {/* Password Screen */}
      {step === 'password' && (
        <form className="flex flex-col gap-4" onSubmit={handlePasswordLogin}>
          <div className="flex items-center gap-2">
            <button
              type="button"
              className="text-blue-600 hover:underline text-sm"
              onClick={() => setStep('email')}
            >
              ← {tCommon('back')}
            </button>
            <span className="text-gray-600 dark:text-gray-300 text-sm">{email}</span>
          </div>

          <input
            type="password"
            placeholder={t('password')}
            className="border rounded px-3 py-2 bg-blue-50 dark:bg-gray-800 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-400"
            value={password}
            onChange={e => setPassword(e.target.value)}
            autoFocus
            required
          />

          <button
            type="submit"
            className="bg-blue-600 text-white rounded px-4 py-2 font-semibold hover:bg-blue-700 shadow disabled:opacity-50"
            disabled={loading || !password}
          >
            {loading ? tCommon('loading') : t('loginButton')}
          </button>

          <div className="text-center">
            <button
              type="button"
              className="text-blue-600 hover:underline text-sm"
              onClick={onForgot}
            >
              {t('forgotPassword')}
            </button>
          </div>
        </form>
      )}

      {/* 2FA Screen */}
      {step === '2fa' && (
        <form className="flex flex-col gap-4" onSubmit={handle2fa}>
          <div className="flex items-center gap-2">
            <button
              type="button"
              className="text-blue-600 hover:underline text-sm"
              onClick={() => setStep('password')}
            >
              ← {tCommon('back')}
            </button>
            <span className="text-gray-600 dark:text-gray-300 text-sm">{email}</span>
          </div>

          <label htmlFor="totp-0" className="font-semibold">{t('twoFactorPrompt')}</label>
          <div className="flex gap-2 justify-center">
            {Array.from({ length: 6 }, (_, index) => (
              <input
                key={index}
                id={`totp-${index}`}
                autoComplete="one-time-code"
                type="text"
                inputMode="numeric"
                pattern="\d"
                maxLength={1}
                required
                className="w-12 h-12 border rounded px-3 py-2 bg-blue-50 dark:bg-gray-800 dark:text-white text-center text-lg font-bold focus:outline-none focus:ring-2 focus:ring-blue-400"
                value={totpCode[index]}
                onChange={e => handleTotpChange(index, e.target.value)}
                onKeyDown={e => handleTotpKeyDown(index, e)}
              />
            ))}
          </div>

          <button
            type="submit"
            className="bg-blue-600 text-white rounded px-4 py-2 font-semibold hover:bg-blue-700 shadow disabled:opacity-50"
            disabled={loading || totpCode.some(digit => digit === '')}
          >
            {loading ? tCommon('loading') : t('verify')}
          </button>

          <div className="text-center">
            <button
              type="button"
              className="text-blue-600 hover:underline text-sm"
              onClick={() => window.open('mailto:support@markpost.dev?subject=Lost 2FA', '_blank')}
            >
              Lost 2FA?
            </button>
          </div>
        </form>
      )}
    </div>
  );
}
