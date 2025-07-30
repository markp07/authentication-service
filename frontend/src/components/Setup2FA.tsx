import React, { useState } from "react";
import Image from "next/image";

interface Setup2FAProps {
  onClose: () => void;
}

const isDev = typeof window !== "undefined" && window.location.hostname === "localhost";
const AUTH_API_BASE = isDev
  ? (process.env.NEXT_PUBLIC_API_URL || "http://localhost:12002/v1")
  : "https://demo.markpost.dev";

export default function Setup2FA({ onClose }: Setup2FAProps) {
  const [qrCodeUrl, setQrCodeUrl] = useState<string | null>(null);
  const [secret, setSecret] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [code, setCode] = useState("");
  const [otpUri, setOtpUri] = useState<string | null>(null);
  const [step, setStep] = useState<"setup" | "confirm" | "done">("setup");
  const [backupCodes, setBackupCodes] = useState<string[]>([]); // Placeholder

  async function setup2fa() {
    setError(null);
    setSuccess(null);
    try {
      const res = await fetch(`${AUTH_API_BASE}/api/auth/v1/2fa/setup`, { method: "POST", credentials: "include" });
      if (res.ok) {
        const data = await res.json();
        setQrCodeUrl(data.qrCodeImage || null);
        setSecret(data.secret || null);
        setOtpUri(data.otpUri || null);
      } else {
        setError("Failed to get 2FA setup info.");
      }
    } catch {
      setError("Network error.");
    }
  }

  async function confirm2fa(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    try {
      const res = await fetch(`${AUTH_API_BASE}/api/auth/v1/2fa/confirm`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ code }),
      });
      if (res.ok) {
        const data = await res.json();
        if (data.code === "TWO_FA_SETUP_SUCCESS") {
          // Placeholder: set backup codes if returned by backend
          setBackupCodes(["ABC123", "DEF456", "GHI789"]); // TODO: Replace with real codes from backend
          setStep("done");
        } else {
          setError("Invalid code. Try again.");
        }
      } else {
        setError("Invalid code. Try again.");
      }
    } catch {
      setError("Network error.");
    }
  }

  React.useEffect(() => {
    setup2fa();
  }, []);

  return (
    <div className="p-6">
      <h2 className="text-xl font-bold mb-4">Setup Two-Factor Authentication</h2>
      {error && <div className="text-red-600 text-sm mb-2">{error}</div>}
      {success && <div className="text-green-600 text-sm mb-2">{success}</div>}
      {step === "setup" && qrCodeUrl && (
        <div className="flex flex-col gap-4 items-center">
          <Image src={qrCodeUrl} alt="Scan this QR code with your authenticator app" width={160} height={160} className="w-40 h-40 border" />
          {otpUri && (
            <a href={otpUri} target="_blank" rel="noopener noreferrer" className="text-blue-600 underline break-all">Open in Authenticator App</a>
          )}
          {secret && (
            <div className="bg-gray-100 dark:bg-gray-800 text-gray-800 dark:text-white rounded px-3 py-2 text-center">
              <span className="font-semibold">Secret:</span> {secret}
            </div>
          )}
          <button className="mt-4 px-4 py-2 rounded bg-blue-600 text-white" onClick={() => setStep("confirm")}>Next</button>
        </div>
      )}
      {step === "confirm" && (
        <form className="flex flex-col gap-4 items-center" onSubmit={confirm2fa}>
          <label htmlFor="topt-code" className="font-semibold">Enter the 6-digit code from your authenticator app:</label>
          <input
            id="topt-code"
            type="text"
            inputMode="numeric"
            pattern="\d{6}"
            maxLength={6}
            required
            className="border rounded px-3 py-2 bg-blue-50 dark:bg-gray-800 dark:text-white text-center text-lg tracking-widest"
            value={code}
            onChange={e => setCode(e.target.value)}
          />
          <button type="submit" className="bg-blue-600 text-white px-4 py-2 rounded">Confirm</button>
        </form>
      )}
      {step === "done" && (
        <div className="flex flex-col gap-4 items-center">
          <div className="text-green-700 font-bold text-lg">2FA setup complete!</div>
          <div className="bg-yellow-100 dark:bg-yellow-900 text-yellow-900 dark:text-yellow-100 rounded px-3 py-2 text-center">
            <span className="font-semibold">Important:</span> Store your backup codes in a safe place. You will need them if you lose access to your authenticator app.
          </div>
          <div className="bg-gray-100 dark:bg-gray-800 text-gray-800 dark:text-white rounded px-3 py-2 text-center">
            <span className="font-semibold">Backup Codes:</span>
            <ul className="mt-2 space-y-1">
              {backupCodes.map(code => (
                <li key={code} className="font-mono text-base">{code}</li>
              ))}
            </ul>
          </div>
        </div>
      )}
      <button className="mt-6 px-4 py-2 rounded bg-gray-300 dark:bg-gray-700" onClick={onClose}>Close</button>
    </div>
  );
}
