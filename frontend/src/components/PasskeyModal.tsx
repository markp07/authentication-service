import React, { useEffect, useState } from "react";
import { AUTH_API_BASE, fetchWithAuthRetry } from "../utils/api";
import { IconFingerprint, IconTrash, IconPlus, IconAlertCircle, IconCheck } from "@tabler/icons-react";
import { useTranslations } from 'next-intl';

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
  const t = useTranslations('passkey');
  const tCommon = useTranslations('common');
  const [passkeys, setPasskeys] = useState<Passkey[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [registering, setRegistering] = useState(false);
  const [registerName, setRegisterName] = useState("");
  const [loginLoading, setLoginLoading] = useState(false);
  const [loginError, setLoginError] = useState<string | null>(null);
  const [user, setUser] = useState<{ email: string } | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    async function fetchUser() {
      try {
        const res = await fetchWithAuthRetry(`${AUTH_API_BASE}/api/auth/v1/user`);
        if (res.ok) {
          setUser(await res.json());
        }
      } catch {}
    }
    if (open) {
      fetchUser();
      fetchPasskeys();
    }
  }, [open]);

  async function fetchPasskeys() {
    setLoading(true);
    setError(null);
    try {
      const res = await fetchWithAuthRetry(`${AUTH_API_BASE}/api/auth/v1/passkey`);
      if (res.ok) {
        setPasskeys(await res.json());
      } else {
        setError(t('loadError'));
      }
    } catch {
      setError(t('networkError'));
    }
    setLoading(false);
  }

  async function handleDelete(credentialId: string) {
    setError(null);
    setSuccess(null);
    try {
      const res = await fetchWithAuthRetry(`${AUTH_API_BASE}/api/auth/v1/passkey/${credentialId}`, {
        method: "DELETE",
      });
      if (res.ok) {
        setPasskeys(passkeys.filter(pk => pk.credentialId !== credentialId));
        setSuccess(t('deleteSuccess'));
        setTimeout(() => setSuccess(null), 3000);
      } else {
        setError(t('deleteError'));
      }
    } catch {
      setError(t('networkError'));
    }
  }

  async function handleRegister(e: React.FormEvent) {
    e.preventDefault();
    setRegistering(true);
    setError(null);
    setSuccess(null);
    try {
      // 1. Get registration options from backend
      const res = await fetchWithAuthRetry(`${AUTH_API_BASE}/api/auth/v1/passkey/register/start`, { method: "POST" });
      const options = await res.json();
      // Decode challenge
      options.challenge = base64urlToUint8Array(options.challenge);
      // Decode user.id
      if (options.user && options.user.id) {
        options.user.id = base64urlToUint8Array(options.user.id);
      }
      // Decode excludeCredentials[].id
      if (Array.isArray(options.excludeCredentials)) {
        options.excludeCredentials = options.excludeCredentials.map((cred: { id: string; type: string; transports?: string[] }) => {
          const decodedId = base64urlToUint8Array(cred.id);
          // Only allow valid AuthenticatorTransport values
          const validTransports = Array.isArray(cred.transports)
            ? cred.transports.filter(t => [
                "usb",
                "nfc",
                "ble",
                "internal",
                "cable",
                "hybrid"
              ].includes(t))
            : undefined;
          return {
            type: cred.type || "public-key",
            id: decodedId,
            transports: validTransports as AuthenticatorTransport[] | undefined,
          };
        });
      }
      // Sanitize extensions: remove keys with null/undefined values
      if (options.extensions && typeof options.extensions === "object") {
        Object.keys(options.extensions).forEach(key => {
          if (options.extensions[key] == null) {
            delete options.extensions[key];
          }
        });
      }
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
        setSuccess(t('registerSuccess'));
        fetchPasskeys();
        setTimeout(() => setSuccess(null), 3000);
      } else {
        setError(t('registerError'));
      }
    } catch (err) {
      console.error(err);
      setError(t('registrationFailed'));
    }
    setRegistering(false);
  }

  async function handleEmailPasskeyLogin() {
    setLoginLoading(true);
    setLoginError(null);
    setSuccess(null);
    try {
      if (!user?.email) {
        setLoginError(t('emailNotLoaded'));
        setLoginLoading(false);
        return;
      }
      // 1. Get authentication options from backend
      const res = await fetchWithAuthRetry(`${AUTH_API_BASE}/api/auth/v1/passkey/login/start`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: user.email })
      });
      if (!res.ok) {
        setLoginError(t('loginStartError'));
        setLoginLoading(false);
        return;
      }
      const options = await res.json();
      options.challenge = base64urlToUint8Array(options.challenge);
      if (options.allowCredentials) {
        options.allowCredentials = options.allowCredentials.map((cred: { id: string; transports?: string[]; type?: string }) => {
          const decodedId = base64urlToUint8Array(cred.id);
          // Only allow valid AuthenticatorTransport values
          const validTransports = Array.isArray(cred.transports)
            ? cred.transports.filter(t => [
                "usb",
                "nfc",
                "ble",
                "internal",
                "cable",
                "hybrid"
              ].includes(t))
            : undefined;
          return {
            type: cred.type || "public-key",
            id: decodedId,
            transports: validTransports as AuthenticatorTransport[] | undefined,
          };
        });
      }
      // 2. Call WebAuthn API
      const assertion = await navigator.credentials.get({ publicKey: options });
      // 3. Send assertion to backend
      const finishRes = await fetchWithAuthRetry(`${AUTH_API_BASE}/api/auth/v1/passkey/login/finish`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: user.email, credential: assertion }),
      });
      if (finishRes.ok) {
        setSuccess(t('loginSuccess'));
        setTimeout(() => {
          setSuccess(null);
          onClose();
        }, 2000);
      } else {
        setLoginError(t('loginError'));
      }
    } catch (err) {
      console.error("Email passkey login error:", err);
      setLoginError(t('loginError'));
    }
    setLoginLoading(false);
  }

  async function handleUsernamelessPasskeyLogin() {
    setLoginLoading(true);
    setLoginError(null);
    setSuccess(null);
    try {
      // 1. Get authentication options from backend
      const res = await fetchWithAuthRetry(`${AUTH_API_BASE}/api/auth/v1/passkey/login/usernameless/start`, {
        method: "POST",
        headers: { "Content-Type": "application/json" }
      });
      if (!res.ok) {
        setLoginError(t('usernamelessStartError'));
        setLoginLoading(false);
        return;
      }
      const options = await res.json();
      options.challenge = base64urlToUint8Array(options.challenge);
      // Sanitize extensions: remove keys with null/undefined values
      if (options.extensions && typeof options.extensions === "object") {
        Object.keys(options.extensions).forEach(key => {
          if (options.extensions[key] == null) {
            delete options.extensions[key];
          }
        });
      }
      // 2. Call WebAuthn API
      const assertion = await navigator.credentials.get({ publicKey: options });
      if (!assertion) {
        setLoginError(t('noPasskeySelected'));
        setLoginLoading(false);
        return;
      }
      // 3. Send assertion to backend
      const finishRes = await fetchWithAuthRetry(`${AUTH_API_BASE}/api/auth/v1/passkey/login/usernameless/finish`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(assertion)
      });
      if (finishRes.ok) {
        setSuccess(t('usernamelessLoginSuccess'));
        setTimeout(() => {
          setSuccess(null);
          onClose();
        }, 2000);
      } else {
        setLoginError(t('usernamelessLoginError'));
      }
    } catch (err) {
      console.error("Usernameless passkey login error:", err);
      setLoginError(t('usernamelessLoginError'));
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
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/20 backdrop-blur-sm">
      <div className="bg-white dark:bg-gray-900 rounded-xl shadow-2xl p-6 min-w-[320px] sm:min-w-[500px] relative max-w-full w-full mx-4 max-h-[90vh] overflow-y-auto">
        <button className="absolute top-4 right-4 text-gray-500 hover:text-gray-900 dark:hover:text-white text-2xl font-bold focus:outline-none" onClick={onClose} aria-label="Close modal">×</button>
        
        <div className="flex flex-col gap-6 pr-8">
          {/* Header */}
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 bg-purple-100 dark:bg-purple-900/30 rounded-lg flex items-center justify-center">
              <IconFingerprint size={24} className="text-purple-600 dark:text-purple-400" />
            </div>
            <div>
              <h2 className="text-2xl font-bold text-gray-900 dark:text-white">{t('title')}</h2>
              <p className="text-sm text-gray-600 dark:text-gray-400">{t('subtitle')}</p>
            </div>
          </div>

          {/* Alerts */}
          {error && (
            <div className="flex items-start gap-3 p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
              <IconAlertCircle size={20} className="text-red-600 dark:text-red-400 flex-shrink-0 mt-0.5" />
              <span className="text-sm text-red-800 dark:text-red-200">{error}</span>
            </div>
          )}
          {success && (
            <div className="flex items-start gap-3 p-4 bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-lg">
              <IconCheck size={20} className="text-green-600 dark:text-green-400 flex-shrink-0 mt-0.5" />
              <span className="text-sm text-green-800 dark:text-green-200">{success}</span>
            </div>
          )}

          {loading ? (
            <div className="flex items-center justify-center py-8">
              <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-purple-500"></div>
            </div>
          ) : (
            <>
              {/* Existing Passkeys */}
              <div>
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-3">{t('yourPasskeys')}</h3>
                {passkeys.length === 0 ? (
                  <div className="bg-gray-50 dark:bg-gray-800 rounded-lg p-6 text-center">
                    <IconFingerprint size={48} className="mx-auto mb-3 text-gray-400" />
                    <p className="text-gray-600 dark:text-gray-400">{t('noPasskeys')}</p>
                    <p className="text-sm text-gray-500 dark:text-gray-500 mt-1">{t('addFirstPasskey')}</p>
                  </div>
                ) : (
                  <div className="space-y-2">
                    {passkeys.map(pk => (
                      <div key={pk.credentialId} className="flex items-center justify-between p-4 bg-gray-50 dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700">
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 bg-purple-100 dark:bg-purple-900/30 rounded-lg flex items-center justify-center">
                            <IconFingerprint size={20} className="text-purple-600 dark:text-purple-400" />
                          </div>
                          <div>
                            <div className="font-medium text-gray-900 dark:text-white">{pk.name}</div>
                            <div className="text-xs text-gray-500 dark:text-gray-400">{t('added')} {new Date(pk.createdAt).toLocaleDateString()}</div>
                          </div>
                        </div>
                        <button 
                          className="flex items-center gap-2 px-3 py-1.5 text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg transition-colors text-sm font-medium"
                          onClick={() => handleDelete(pk.credentialId)}
                        >
                          <IconTrash size={16} />
                          {t('delete')}
                        </button>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Register New Passkey */}
              <div className="border-t border-gray-200 dark:border-gray-700 pt-6">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-3">{t('addNewPasskey')}</h3>
                <form className="flex flex-col gap-4" onSubmit={handleRegister}>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                      {t('deviceName')}
                    </label>
                    <input 
                      type="text" 
                      placeholder={t('deviceNamePlaceholder')} 
                      className="w-full px-4 py-2.5 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-purple-500 focus:border-transparent bg-white dark:bg-gray-800 text-gray-900 dark:text-white" 
                      value={registerName} 
                      onChange={e => setRegisterName(e.target.value)} 
                      required 
                      disabled={registering} 
                    />
                  </div>
                  <button 
                    type="submit" 
                    className="flex items-center justify-center gap-2 w-full px-4 py-2.5 bg-purple-600 hover:bg-purple-700 text-white rounded-lg font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed" 
                    disabled={registering || !registerName}
                  >
                    <IconPlus size={18} />
                    {registering ? t('registering') : t('registerPasskey')}
                  </button>
                </form>
              </div>

              {/* Test Login (Optional) */}
              <div className="border-t border-gray-200 dark:border-gray-700 pt-6">
                <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-3">{t('testLogin')}</h3>
                <div className="flex flex-col gap-3">
                  <button
                    className="w-full px-4 py-2.5 bg-green-600 hover:bg-green-700 text-white rounded-lg font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    onClick={handleEmailPasskeyLogin}
                    disabled={loginLoading || passkeys.length === 0}
                  >
                    {loginLoading ? t('authenticating') : t('loginWithEmail')}
                  </button>
                  <button
                    className="w-full px-4 py-2.5 bg-purple-600 hover:bg-purple-700 text-white rounded-lg font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                    onClick={handleUsernamelessPasskeyLogin}
                    disabled={loginLoading || passkeys.length === 0}
                  >
                    {loginLoading ? t('authenticating') : t('loginUsernameless')}
                  </button>
                </div>
                {loginError && (
                  <div className="mt-2 text-sm text-red-600 dark:text-red-400">{loginError}</div>
                )}
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
