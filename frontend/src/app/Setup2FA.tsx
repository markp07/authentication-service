import React, { useState } from "react";

interface Setup2FAProps {
  onClose: () => void;
}

const API_BASE = "http://localhost:12002/v1";

export default function Setup2FA({ onClose }: Setup2FAProps) {
  const [qrCodeUrl, setQrCodeUrl] = useState<string | null>(null);
  const [secret, setSecret] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [code, setCode] = useState("");

  async function setup2fa() {
    setError(null);
    setSuccess(null);
    try {
      const res = await fetch(`${API_BASE}/auth/2fa/setup`, { method: "POST", credentials: "include" });
      if (res.ok) {
        const data = await res.json();
        // If qrCodeUrl is a data URL, use it directly. If it's a backend endpoint, prefix with API_BASE
        let qrUrl = data.qrCodeUrl;
        if (qrUrl && !qrUrl.startsWith("data:")) {
          // If qrCodeUrl is a relative path, prefix with API_BASE
          if (qrUrl.startsWith("/")) {
            qrUrl = `${API_BASE}${qrUrl}`;
          }
        }
        setQrCodeUrl(qrUrl);
        setSecret(data.secret);
      } else {
        setError("Failed to get 2FA setup info.");
      }
    } catch {
      setError("Network error.");
    }
  }

  async function verify2fa(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    try {
      const res = await fetch(`${API_BASE}/auth/2fa/verify`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ code }),
      });
      if (res.ok) {
        setSuccess("2FA enabled!");
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
    <div className="flex flex-col gap-4">
      <h2 className="text-xl font-bold mb-2">Setup Two-Factor Authentication</h2>
      {error && <div className="text-red-600 text-sm bg-red-100 rounded px-2 py-1">{error}</div>}
      {success && <div className="text-green-600 text-sm bg-green-100 rounded px-2 py-1">{success}</div>}
      {qrCodeUrl && (
        <div className="flex flex-col items-center gap-2">
          <img src={qrCodeUrl} alt="Scan QR code" className="w-40 h-40" />
          <div className="text-xs text-gray-700">Secret: {secret}</div>
        </div>
      )}
      <form className="flex flex-col gap-2" onSubmit={verify2fa}>
        <input
          type="text"
          placeholder="Enter code from authenticator app"
          className="border rounded px-3 py-2 bg-blue-50 focus:outline-none focus:ring-2 focus:ring-blue-400"
          required
          value={code}
          onChange={e => setCode(e.target.value)}
        />
        <button
          type="submit"
          className="bg-blue-600 text-white rounded px-4 py-2 font-semibold"
        >
          Verify & Enable 2FA
        </button>
      </form>
      <button className="bg-gray-600 text-white rounded px-4 py-2 font-semibold" onClick={onClose}>
        Close
      </button>
    </div>
  );
}
