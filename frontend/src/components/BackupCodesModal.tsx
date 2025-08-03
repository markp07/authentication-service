import React, { useState } from "react";
import { AUTH_API_BASE, fetchWithAuthRetry } from "../utils/api";
import Modal from "./Modal";

interface BackupCodesModalProps {
  open: boolean;
  onClose: () => void;
}

export default function BackupCodesModal({ open, onClose }: BackupCodesModalProps) {
  const [codes, setCodes] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleGenerate() {
    setLoading(true);
    setError(null);
    try {
      const res = await fetchWithAuthRetry(`${AUTH_API_BASE}/api/auth/v1/2fa/backup-code`, { method: "POST" });
      if (res.ok) {
        const data = await res.json();
        setCodes(data.backupCode || []);
      } else {
        setError("Failed to generate backup codes.");
      }
    } catch {
      setError("Network error.");
    }
    setLoading(false);
  }

  React.useEffect(() => {
    if (open) {
      handleGenerate();
    } else {
      setCodes([]);
      setError(null);
    }
  }, [open]);

  return (
    <Modal open={open} onClose={onClose}>
      <div className="flex flex-col gap-4 items-center p-4">
        <h2 className="text-xl font-bold mb-2">Backup Codes</h2>
        {loading ? (
          <div className="text-blue-600">Generating...</div>
        ) : error ? (
          <div className="text-red-600 text-sm">{error}</div>
        ) : codes.length > 0 ? (
          <div className="flex flex-col gap-2 items-center">
            <div className="text-gray-700 dark:text-gray-200 text-center mb-2">Store these codes safely. Each code can be used once if you lose access to your authenticator app.</div>
            <div className="grid grid-cols-1 gap-2">
              {codes.map((code, i) => (
                <div key={i} className="bg-gray-100 dark:bg-gray-800 rounded px-3 py-2 font-mono text-lg text-center">
                  {code}
                </div>
              ))}
            </div>
          </div>
        ) : null}
        <button className="bg-gray-600 text-white rounded px-4 py-2 font-semibold mt-4" onClick={onClose}>Close</button>
      </div>
    </Modal>
  );
}
