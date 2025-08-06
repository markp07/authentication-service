import React, { useEffect, useState } from "react";
import { AUTH_API_BASE, fetchWithAuthRetry } from "../utils/api";

interface Passkey {
  credentialId: string;
  name: string;
  createdAt: string;
}

interface PasskeyModalProps {
  open: boolean;
  onClose: () => void;
}

export default function PasskeyModal({ open, onClose }: PasskeyModalProps) {
  const [passkeys, setPasskeys] = useState<Passkey[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [registering, setRegistering] = useState(false);
  const [registerName, setRegisterName] = useState("");
  const [loginLoading, setLoginLoading] = useState(false);
  const [loginError, setLoginError] = useState<string | null>(null);

  useEffect(() => {
    if (!open) return;
    fetchPasskeys();
  }, [open]);

  async function fetchPasskeys() {
    setLoading(true);
    setError(null);
    try {
      const res = await fetchWithAuthRetry(`${AUTH_API_BASE}/api/auth/v1/passkey`);
      if (res.ok) {
        setPasskeys(await res.json());
      } else {
        setError("Failed to load passkeys.");
      }
    } catch {
      setError("Network error.");
    }
    setLoading(false);
  }

  async function handleDelete(credentialId: string) {
    setLoading(true);
    setError(null);
    try {
      const res = await fetchWithAuthRetry(`${AUTH_API_BASE}/api/auth/v1/passkey/${credentialId}`, {
        method: "DELETE",
      });
      if (res.ok) {
        setPasskeys(passkeys.filter(pk => pk.credentialId !== credentialId));
      } else {
        setError("Failed to delete passkey.");
      }
    } catch {
      setError("Network error.");
    }
    setLoading(false);
  }

  async function handleRegister(e: React.FormEvent) {
    e.preventDefault();
    setRegistering(true);
    setError(null);
    try {
      // 1. Get registration options from backend
      const res = await fetchWithAuthRetry(`${AUTH_API_BASE}/api/auth/v1/passkey/register/start`, { method: "POST" });
      const options = await res.json();
      options.challenge = base64urlToUint8Array(options.challenge);
      options.user.id = base64urlToUint8Array(options.user.id);
      // 2. Call WebAuthn API
      const credential = await navigator.credentials.create({ publicKey: options });
      // 3. Send credential to backend
      const finishRes = await fetchWithAuthRetry(`${AUTH_API_BASE}/api/auth/v1/passkey/register/finish?name=${encodeURIComponent(registerName)}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(credential),
      });
      if (finishRes.ok) {
        setRegisterName("");
        fetchPasskeys();
      } else {
        setError("Failed to register passkey.");
      }
    } catch (err) {
      setError("Passkey registration failed.");
    }
    setRegistering(false);
  }

  async function handleLogin() {
    setLoginLoading(true);
    setLoginError(null);
    try {
      // 1. Get authentication options from backend
      const res = await fetchWithAuthRetry(`${AUTH_API_BASE}/api/auth/v1/passkey/login/start?email=`, { method: "POST" });
      const options = await res.json();
      options.challenge = Uint8Array.from(atob(options.challenge), c => c.charCodeAt(0));
      options.allowCredentials = options.allowCredentials?.map((cred: { id: string } & Record<string, unknown>) => ({ ...cred, id: Uint8Array.from(atob(cred.id), c => c.charCodeAt(0)) }));
      // 2. Call WebAuthn API
      const assertion = await navigator.credentials.get({ publicKey: options });
      // 3. Send assertion to backend
      const finishRes = await fetchWithAuthRetry(`${AUTH_API_BASE}/api/auth/v1/passkey/login/finish?email=`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(assertion),
      });
      if (finishRes.ok) {
        onClose(); // or set login state
      } else {
        setLoginError("Passkey login failed.");
      }
    } catch (err) {
      setLoginError("Passkey login failed.");
    }
    setLoginLoading(false);
  }

  function base64urlToUint8Array(base64url: string) {
    let base64 = base64url.replace(/-/g, '+').replace(/_/g, '/');
    while (base64.length % 4) base64 += '=';
    return Uint8Array.from(atob(base64), c => c.charCodeAt(0));
  }

  if (!open) return null;
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-40 backdrop-blur-sm">
      <div className="bg-white dark:bg-gray-900 rounded-lg shadow-2xl p-6 min-w-[320px] relative max-w-full w-full sm:w-auto mx-2">
        <button className="absolute top-2 right-2 text-gray-500 hover:text-gray-900 dark:hover:text-white text-2xl font-bold focus:outline-none" onClick={onClose} aria-label="Close modal">×</button>
        <h2 className="text-xl font-bold mb-2">Configure Passkey</h2>
        {error && <div className="text-red-600 text-sm bg-red-100 rounded px-2 py-1 mb-2">{error}</div>}
        {loading ? (
          <div className="text-blue-600">Loading...</div>
        ) : (
          <>
            <div className="mb-4">
              <div className="font-semibold mb-1">Your Passkeys:</div>
              {passkeys.length === 0 ? (
                <div className="text-gray-600">No passkeys configured.</div>
              ) : (
                <ul className="mb-2">
                  {passkeys.map(pk => (
                    <li key={pk.credentialId} className="flex items-center justify-between py-1">
                      <span>{pk.name} <span className="text-xs text-gray-500">({new Date(pk.createdAt).toLocaleString()})</span></span>
                      <button className="text-red-600 text-xs ml-2" onClick={() => handleDelete(pk.credentialId)}>Delete</button>
                    </li>
                  ))}
                </ul>
              )}
            </div>
            <form className="mb-4" onSubmit={handleRegister}>
              <div className="font-semibold mb-1">Register New Passkey:</div>
              <input type="text" placeholder="Device name" className="border rounded px-2 py-1 mb-2 w-full" value={registerName} onChange={e => setRegisterName(e.target.value)} required disabled={registering} />
              <button type="submit" className="bg-blue-600 text-white rounded px-4 py-2 font-semibold" disabled={registering || !registerName}>{registering ? "Registering..." : "Register Passkey"}</button>
            </form>
            <div className="mb-2">
              <button className="bg-green-600 text-white rounded px-4 py-2 font-semibold" onClick={handleLogin} disabled={loginLoading}>{loginLoading ? "Logging in..." : "Login with Passkey"}</button>
              {loginError && <div className="text-red-600 text-xs mt-1">{loginError}</div>}
            </div>
          </>
        )}
      </div>
    </div>
  );
}
