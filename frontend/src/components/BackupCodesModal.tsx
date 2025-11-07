import React, { useState } from "react";
import { AUTH_API_BASE, fetchWithAuthRetry } from "../utils/api";
import Modal from "./Modal";
import { IconKey, IconCopy, IconCheck, IconAlertCircle } from "@tabler/icons-react";

interface BackupCodesModalProps {
  open: boolean;
  onClose: () => void;
}

export default function BackupCodesModal({ open, onClose }: BackupCodesModalProps) {
  const [codes, setCodes] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [copied, setCopied] = useState(false);

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

  async function handleCopy() {
    if (codes.length > 0) {
      await navigator.clipboard.writeText(codes.join("\n"));
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  }

  React.useEffect(() => {
    if (open) {
      handleGenerate();
      setCopied(false);
    } else {
      setCodes([]);
      setError(null);
    }
  }, [open]);

  return (
    <Modal open={open} onClose={onClose}>
      <div className="flex flex-col gap-6 min-w-[320px] sm:min-w-[480px]">
        {/* Header */}
        <div className="flex items-center gap-3">
          <div className="w-12 h-12 bg-blue-100 dark:bg-blue-900/30 rounded-lg flex items-center justify-center">
            <IconKey size={24} className="text-blue-600 dark:text-blue-400" />
          </div>
          <div>
            <h2 className="text-2xl font-bold text-gray-900 dark:text-white">Backup Codes</h2>
            <p className="text-sm text-gray-600 dark:text-gray-400">Emergency access codes</p>
          </div>
        </div>

        {loading ? (
          <div className="flex items-center justify-center py-8">
            <div className="animate-spin rounded-full h-10 w-10 border-t-2 border-b-2 border-blue-500"></div>
          </div>
        ) : error ? (
          <div className="flex items-start gap-3 p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
            <IconAlertCircle size={20} className="text-red-600 dark:text-red-400 flex-shrink-0 mt-0.5" />
            <span className="text-sm text-red-800 dark:text-red-200">{error}</span>
          </div>
        ) : codes.length > 0 ? (
          <div className="flex flex-col gap-4">
            <div className="bg-amber-50 dark:bg-amber-900/20 border border-amber-200 dark:border-amber-800 rounded-lg p-4">
              <div className="flex items-start gap-3">
                <IconAlertCircle size={20} className="text-amber-600 dark:text-amber-400 flex-shrink-0 mt-0.5" />
                <div>
                  <div className="font-semibold text-amber-900 dark:text-amber-200 mb-1">Important</div>
                  <p className="text-sm text-amber-800 dark:text-amber-300">
                    Store these codes safely. Each code can be used once if you lose access to your authenticator app.
                  </p>
                </div>
              </div>
            </div>

            <div className="bg-gray-50 dark:bg-gray-800 rounded-lg p-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                {codes.map((code, i) => (
                  <div key={i} className="bg-white dark:bg-gray-700 rounded-lg px-4 py-3 font-mono text-center text-gray-900 dark:text-white border border-gray-200 dark:border-gray-600">
                    {code}
                  </div>
                ))}
              </div>
            </div>

            <button
              onClick={handleCopy}
              className="flex items-center justify-center gap-2 w-full px-4 py-2.5 bg-gray-200 hover:bg-gray-300 dark:bg-gray-700 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300 rounded-lg font-medium transition-colors"
            >
              {copied ? (
                <>
                  <IconCheck size={18} />
                  Copied to Clipboard!
                </>
              ) : (
                <>
                  <IconCopy size={18} />
                  Copy All Codes
                </>
              )}
            </button>
          </div>
        ) : null}

        <button 
          className="w-full px-4 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium transition-colors" 
          onClick={onClose}
        >
          Close
        </button>
      </div>
    </Modal>
  );
}
