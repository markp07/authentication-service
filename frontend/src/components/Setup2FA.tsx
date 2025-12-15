import React, { useState } from "react";
import Image from "next/image";
import { AUTH_API_BASE, fetchWithAuthRetry } from "../utils/api";
import { IconShield, IconCheck, IconAlertCircle, IconKey, IconQrcode } from "@tabler/icons-react";
import { useTranslations } from 'next-intl';

export default function Setup2FA() {
  const t = useTranslations('setup2FA');
  const [qrCodeUrl, setQrCodeUrl] = useState<string | null>(null);
  const [secret, setSecret] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [code, setCode] = useState("");
  const [otpUri, setOtpUri] = useState<string | null>(null);
  const [step, setStep] = useState<"setup" | "confirm" | "done">("setup");
  const [backupCode, setBackupCode] = useState<string | null>(null);

  const setup2fa = React.useCallback(async () => {
    setError(null);
    setSuccess(null);
    try {
      const res = await fetchWithAuthRetry(`${AUTH_API_BASE}/api/auth/v1/2fa/setup`, { method: "POST" });
      if (res.ok) {
        const data = await res.json();
        setQrCodeUrl(data.qrCodeImage || null);
        setSecret(data.secret || null);
        setOtpUri(data.otpUri || null);
      } else {
        setError(t('setupError'));
      }
    } catch {
      setError(t('networkError'));
    }
  }, [t]);

  async function fetchBackupCode() {
    try {
      const res = await fetchWithAuthRetry(`${AUTH_API_BASE}/api/auth/v1/2fa/backup-code`, { method: "POST" });
      if (res.ok) {
        const data = await res.json();
        setBackupCode(data.backupCode || null);
      } else {
        setBackupCode(null);
      }
    } catch {
      setBackupCode(null);
    }
  }

  async function confirm2fa(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    try {
      const res = await fetchWithAuthRetry(`${AUTH_API_BASE}/api/auth/v1/2fa/confirm`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ code }),
      });
      if (res.ok) {
        const data = await res.json();
        if (data.code === "TWO_FA_SETUP_SUCCESS") {
          await fetchBackupCode();
          setStep("done");
        } else {
          setError(t('invalidCode'));
        }
      } else {
        setError(t('invalidCode'));
      }
    } catch {
      setError(t('networkError'));
    }
  }

  React.useEffect(() => {
    setup2fa();
  }, [setup2fa]);

  return (
    <div className="flex flex-col gap-6 min-w-[320px] sm:min-w-[480px] max-w-[500px]">
      {/* Header */}
      <div className="flex items-center gap-3">
        <div className="w-12 h-12 bg-green-100 dark:bg-green-900/30 rounded-lg flex items-center justify-center">
          <IconShield size={24} className="text-green-600 dark:text-green-400" />
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

      {/* Step 1: QR Code */}
      {step === "setup" && qrCodeUrl && (
        <div className="flex flex-col gap-4">
          <div className="bg-gray-50 dark:bg-gray-800 rounded-lg p-6 flex flex-col items-center gap-4">
            <div className="flex items-center gap-2 text-gray-700 dark:text-gray-300">
              <IconQrcode size={20} />
              <span className="font-medium">{t('scanQRCode')}</span>
            </div>
            <div className="bg-white p-4 rounded-lg shadow-sm">
              <Image src={qrCodeUrl} alt={t('scanQRCodeAlt')} width={200} height={200} className="w-48 h-48" />
            </div>
            {otpUri && (
              <a href={otpUri} target="_blank" rel="noopener noreferrer" className="text-blue-600 dark:text-blue-400 hover:underline text-sm">
                {t('openInApp')}
              </a>
            )}
          </div>
          {secret && (
            <div className="bg-amber-50 dark:bg-amber-900/20 border border-amber-200 dark:border-amber-800 rounded-lg p-4">
              <div className="flex items-start gap-3">
                <IconKey size={20} className="text-amber-600 dark:text-amber-400 flex-shrink-0 mt-0.5" />
                <div>
                  <div className="font-semibold text-amber-900 dark:text-amber-200 mb-1">{t('manualEntry')}</div>
                  <div className="text-sm text-amber-800 dark:text-amber-300 mb-2">{t('manualEntryDescription')}</div>
                  <div className="bg-white dark:bg-gray-800 rounded px-3 py-2 font-mono text-sm break-all">
                    {secret}
                  </div>
                </div>
              </div>
            </div>
          )}
          <button 
            className="w-full px-4 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium transition-colors" 
            onClick={() => setStep("confirm")}
          >
            {t('nextVerifyCode')}
          </button>
        </div>
      )}

      {/* Step 2: Verify */}
      {step === "confirm" && (
        <form className="flex flex-col gap-4" onSubmit={confirm2fa}>
          <div className="bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg p-4">
            <p className="text-sm text-blue-800 dark:text-blue-200">
              {t('enterCodeDescription')}
            </p>
          </div>
          <div>
            <label htmlFor="totp-code" className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              {t('verificationCode')}
            </label>
            <input
              id="totp-code"
              type="text"
              inputMode="numeric"
              pattern="\d{6}"
              maxLength={6}
              required
              className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-white dark:bg-gray-800 text-gray-900 dark:text-white text-center text-2xl tracking-widest font-mono"
              placeholder="000000"
              value={code}
              onChange={e => setCode(e.target.value)}
            />
          </div>
          <button 
            type="submit" 
            className="w-full px-4 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium transition-colors"
          >
            {t('verifyAndEnable')}
          </button>
        </form>
      )}

      {/* Step 3: Success with Backup Code */}
      {step === "done" && (
        <div className="flex flex-col gap-4">
          <div className="flex items-center justify-center py-6">
            <div className="w-16 h-16 bg-green-100 dark:bg-green-900/30 rounded-full flex items-center justify-center">
              <IconCheck size={32} className="text-green-600 dark:text-green-400" />
            </div>
          </div>
          <div className="text-center">
            <h3 className="text-xl font-bold text-gray-900 dark:text-white mb-2">{t('successTitle')}</h3>
            <p className="text-sm text-gray-600 dark:text-gray-400">{t('successDescription')}</p>
          </div>
          {backupCode && (
            <div className="bg-amber-50 dark:bg-amber-900/20 border border-amber-200 dark:border-amber-800 rounded-lg p-4">
              <div className="flex items-start gap-3">
                <IconAlertCircle size={20} className="text-amber-600 dark:text-amber-400 flex-shrink-0 mt-0.5" />
                <div className="flex-1">
                  <div className="font-semibold text-amber-900 dark:text-amber-200 mb-1">{t('backupCodeTitle')}</div>
                  <p className="text-sm text-amber-800 dark:text-amber-300 mb-3">
                    {t('backupCodeDescription')}
                  </p>
                  <div className="bg-white dark:bg-gray-800 rounded-lg px-4 py-3">
                    <div className="font-mono text-lg text-center text-gray-900 dark:text-white tracking-wider">
                      {backupCode.match(/.{1,6}/g)?.join(" - ")}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
