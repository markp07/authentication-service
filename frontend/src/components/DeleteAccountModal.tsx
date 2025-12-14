import React from "react";
import { AUTH_API_BASE, fetchWithAuthRetry } from "../utils/api";
import { useTranslations } from 'next-intl';

interface DeleteAccountModalProps {
  onSuccess: () => void;
  onCancel: () => void;
}

const DeleteAccountModal: React.FC<DeleteAccountModalProps> = ({ onSuccess, onCancel }) => {
  const t = useTranslations('profile');
  const tCommon = useTranslations('common');
  const [password, setPassword] = React.useState("");
  const [error, setError] = React.useState<string | null>(null);
  const [loading, setLoading] = React.useState(false);

  async function handleDelete() {
    setLoading(true);
    setError(null);
    try {
      const res = await fetchWithAuthRetry(`${AUTH_API_BASE}/api/auth/v1/user`, {
        method: "DELETE",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ password }),
      });
      if (res.ok) {
        onSuccess();
      } else {
        const data = await res.json();
        setError(data.message || t('deleteAccountError'));
      }
    } catch {
      setError(t('deleteAccountNetworkError'));
    }
    setLoading(false);
  }

  return (
    <div className="flex flex-col gap-4 items-center p-6">
      <h2 className="text-xl font-bold mb-2 text-red-700">{t('deleteAccount')}</h2>
      <p className="text-center">{t('deleteAccountConfirm')} {t('deleteAccountWarning')}</p>
      <input
        type="password"
        className="border rounded px-3 py-2 w-full max-w-xs"
        placeholder={t('enterPassword')}
        value={password}
        onChange={e => setPassword(e.target.value)}
        disabled={loading}
      />
      {error && <div className="text-red-600 text-sm">{error}</div>}
      <button className="bg-red-600 text-white rounded px-4 py-2 font-semibold w-full max-w-xs" onClick={handleDelete} disabled={loading || !password}>
        {loading ? t('deleting') : t('deleteAccount')}
      </button>
      <button className="bg-gray-600 text-white rounded px-4 py-2 font-semibold w-full max-w-xs" onClick={onCancel} disabled={loading}>{tCommon('cancel')}</button>
    </div>
  );
};

export default DeleteAccountModal;
