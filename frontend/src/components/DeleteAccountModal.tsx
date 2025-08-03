import React from "react";
import { AUTH_API_BASE, fetchWithAuthRetry } from "../utils/api";

interface DeleteAccountModalProps {
  onSuccess: () => void;
  onCancel: () => void;
}

const DeleteAccountModal: React.FC<DeleteAccountModalProps> = ({ onSuccess, onCancel }) => {
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
        setError(data.message || "Failed to delete account.");
      }
    } catch {
      setError("Network error. Please try again.");
    }
    setLoading(false);
  }

  return (
    <div className="flex flex-col gap-4 items-center p-6">
      <h2 className="text-xl font-bold mb-2 text-red-700">Delete Account</h2>
      <p className="text-center">Are you sure you want to delete your account? This action cannot be undone.</p>
      <input
        type="password"
        className="border rounded px-3 py-2 w-full max-w-xs"
        placeholder="Enter your password"
        value={password}
        onChange={e => setPassword(e.target.value)}
        disabled={loading}
      />
      {error && <div className="text-red-600 text-sm">{error}</div>}
      <button className="bg-red-600 text-white rounded px-4 py-2 font-semibold w-full max-w-xs" onClick={handleDelete} disabled={loading || !password}>
        {loading ? "Deleting..." : "Delete Account"}
      </button>
      <button className="bg-gray-600 text-white rounded px-4 py-2 font-semibold w-full max-w-xs" onClick={onCancel} disabled={loading}>Cancel</button>
    </div>
  );
};

export default DeleteAccountModal;
