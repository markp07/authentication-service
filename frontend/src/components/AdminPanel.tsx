'use client';

import React, { useEffect, useState } from "react";
import { useTranslations } from 'next-intl';
import { AUTH_API_BASE, fetchWithAuthRetry } from "../utils/api";
import {
  IconShieldCog,
  IconLock,
  IconLockOpen,
  IconMail,
  IconEye,
  IconTrash,
  IconPlus,
  IconX,
} from "@tabler/icons-react";

interface AdminUser {
  id: string;
  userName: string;
  email: string;
  createdAt?: string;
  emailVerified?: boolean;
  twoFactorEnabled?: boolean;
  passkeyEnabled?: boolean;
  blocked?: boolean;
  roles?: string[];
}

interface AdminUserDetails extends AdminUser {
  updatedAt?: string;
  failedLoginAttempts?: number;
  accountLockedUntil?: string;
}

export default function AdminPanel() {
  const t = useTranslations('admin');
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedUser, setSelectedUser] = useState<AdminUserDetails | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [actionMessage, setActionMessage] = useState<{ text: string; ok: boolean } | null>(null);
  const [newRole, setNewRole] = useState("");

  useEffect(() => {
    fetchUsers();
  }, []);

  async function fetchUsers() {
    setLoading(true);
    setError(null);
    try {
      const res = await fetchWithAuthRetry(`${AUTH_API_BASE}/api/auth/v1/admin/users`);
      if (res.ok) {
        setUsers(await res.json());
      } else {
        setError(t('failedToLoad'));
      }
    } catch {
      setError(t('networkError'));
    }
    setLoading(false);
  }

  async function openDetails(id: string) {
    setDetailLoading(true);
    setSelectedUser(null);
    setActionMessage(null);
    setNewRole("");
    try {
      const res = await fetchWithAuthRetry(`${AUTH_API_BASE}/api/auth/v1/admin/users/${id}`);
      if (res.ok) {
        setSelectedUser(await res.json());
      }
    } catch {
      // ignore
    }
    setDetailLoading(false);
  }

  function notify(text: string, ok: boolean) {
    setActionMessage({ text, ok });
  }

  async function handleBlock(id: string, block: boolean) {
    const endpoint = block ? "block" : "unblock";
    try {
      const res = await fetchWithAuthRetry(`${AUTH_API_BASE}/api/auth/v1/admin/users/${id}/${endpoint}`, {
        method: "PUT",
      });
      if (res.ok) {
        notify(block ? t('blockSuccess') : t('unblockSuccess'), true);
        await fetchUsers();
        if (selectedUser?.id === id) {
          setSelectedUser(prev => prev ? { ...prev, blocked: block } : prev);
        }
      } else {
        notify(block ? t('blockError') : t('unblockError'), false);
      }
    } catch {
      notify(t('networkError'), false);
    }
  }

  async function handleSendResetLink(id: string) {
    try {
      const res = await fetchWithAuthRetry(`${AUTH_API_BASE}/api/auth/v1/admin/users/${id}/send-reset-link`, {
        method: "POST",
      });
      notify(res.ok ? t('resetLinkSent') : t('resetLinkError'), res.ok);
    } catch {
      notify(t('networkError'), false);
    }
  }

  async function handleAddRole(id: string) {
    if (!newRole.trim()) return;
    try {
      const res = await fetchWithAuthRetry(`${AUTH_API_BASE}/api/auth/v1/admin/users/${id}/roles`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ role: newRole.trim() }),
      });
      if (res.ok) {
        notify(t('addRoleSuccess'), true);
        setNewRole("");
        await fetchUsers();
        await openDetails(id);
      } else {
        notify(t('addRoleError'), false);
      }
    } catch {
      notify(t('networkError'), false);
    }
  }

  async function handleRemoveRole(id: string, role: string) {
    try {
      const res = await fetchWithAuthRetry(
        `${AUTH_API_BASE}/api/auth/v1/admin/users/${id}/roles/${encodeURIComponent(role)}`,
        { method: "DELETE" }
      );
      if (res.ok) {
        notify(t('removeRoleSuccess'), true);
        await fetchUsers();
        await openDetails(id);
      } else {
        notify(t('removeRoleError'), false);
      }
    } catch {
      notify(t('networkError'), false);
    }
  }

  function formatDate(iso?: string) {
    if (!iso) return t('notAvailable');
    return new Date(iso).toLocaleString();
  }

  function bool(val?: boolean) {
    if (val === undefined || val === null) return t('notAvailable');
    return val ? t('yes') : t('no');
  }

  return (
    <div className="p-6 max-w-7xl mx-auto">
      {/* Header */}
      <div className="flex items-center gap-3 mb-6">
        <IconShieldCog size={32} className="text-blue-600 dark:text-blue-400" />
        <h1 className="text-2xl font-bold text-gray-900 dark:text-white">{t('title')}</h1>
      </div>

      {/* Action feedback banner */}
      {actionMessage && (
        <div className={`mb-4 px-4 py-3 rounded-lg flex items-center justify-between ${
          actionMessage.ok
            ? "bg-green-50 dark:bg-green-900/20 text-green-700 dark:text-green-400 border border-green-200 dark:border-green-800"
            : "bg-red-50 dark:bg-red-900/20 text-red-700 dark:text-red-400 border border-red-200 dark:border-red-800"
        }`}>
          <span>{actionMessage.text}</span>
          <button onClick={() => setActionMessage(null)}>
            <IconX size={16} />
          </button>
        </div>
      )}

      {/* Loading / Error */}
      {loading && (
        <div className="flex items-center gap-2 text-gray-500 dark:text-gray-400">
          <div className="animate-spin rounded-full h-5 w-5 border-t-2 border-b-2 border-blue-500"></div>
          <span>{t('loading')}</span>
        </div>
      )}
      {error && (
        <div className="text-red-600 dark:text-red-400 bg-red-50 dark:bg-red-900/20 px-4 py-3 rounded-lg">
          {error}
        </div>
      )}

      {/* Users table */}
      {!loading && !error && (
        <div className="bg-white dark:bg-gray-800 rounded-xl shadow overflow-x-auto">
          <table className="w-full text-sm text-left">
            <thead className="text-xs uppercase bg-gray-50 dark:bg-gray-700 text-gray-600 dark:text-gray-300">
              <tr>
                <th className="px-4 py-3">{t('username')}</th>
                <th className="px-4 py-3">{t('email')}</th>
                <th className="px-4 py-3">{t('createdAt')}</th>
                <th className="px-4 py-3">{t('emailVerified')}</th>
                <th className="px-4 py-3">{t('twoFactorEnabled')}</th>
                <th className="px-4 py-3">{t('passkeyEnabled')}</th>
                <th className="px-4 py-3">{t('status')}</th>
                <th className="px-4 py-3">{t('actions')}</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 dark:divide-gray-700">
              {users.map(user => (
                <tr key={user.id} className="hover:bg-gray-50 dark:hover:bg-gray-750">
                  <td className="px-4 py-3 font-medium text-gray-900 dark:text-white">{user.userName}</td>
                  <td className="px-4 py-3 text-gray-600 dark:text-gray-300">{user.email}</td>
                  <td className="px-4 py-3 text-gray-600 dark:text-gray-300 whitespace-nowrap">{formatDate(user.createdAt)}</td>
                  <td className="px-4 py-3">
                    <StatusBadge value={user.emailVerified} trueLabel={t('yes')} falseLabel={t('no')} />
                  </td>
                  <td className="px-4 py-3">
                    <StatusBadge value={user.twoFactorEnabled} trueLabel={t('yes')} falseLabel={t('no')} />
                  </td>
                  <td className="px-4 py-3">
                    <StatusBadge value={user.passkeyEnabled} trueLabel={t('yes')} falseLabel={t('no')} />
                  </td>
                  <td className="px-4 py-3">
                    {user.blocked
                      ? <span className="px-2 py-1 text-xs rounded-full bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-400">{t('blocked')}</span>
                      : <span className="px-2 py-1 text-xs rounded-full bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400">{t('active')}</span>
                    }
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-1">
                      <ActionButton icon={<IconEye size={15} />} label={t('viewDetails')} onClick={() => openDetails(user.id)} />
                      <ActionButton
                        icon={user.blocked ? <IconLockOpen size={15} /> : <IconLock size={15} />}
                        label={user.blocked ? t('unblockAccount') : t('blockAccount')}
                        onClick={() => handleBlock(user.id, !user.blocked)}
                        danger={!user.blocked}
                      />
                      <ActionButton icon={<IconMail size={15} />} label={t('sendResetLink')} onClick={() => handleSendResetLink(user.id)} />
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* User detail modal */}
      {(selectedUser || detailLoading) && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4" onClick={() => setSelectedUser(null)}>
          <div
            className="bg-white dark:bg-gray-800 rounded-xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto"
            onClick={e => e.stopPropagation()}
          >
            <div className="flex items-center justify-between p-6 border-b border-gray-200 dark:border-gray-700">
              <h2 className="text-lg font-semibold text-gray-900 dark:text-white">{t('userDetails')}</h2>
              <button
                onClick={() => setSelectedUser(null)}
                className="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
                aria-label="Close"
              >
                <IconX size={20} />
              </button>
            </div>

            {detailLoading ? (
              <div className="p-8 flex justify-center">
                <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-blue-500"></div>
              </div>
            ) : selectedUser && (
              <div className="p-6 space-y-5">
                {/* Action feedback inside modal */}
                {actionMessage && (
                  <div className={`px-3 py-2 rounded text-sm ${actionMessage.ok ? "bg-green-50 text-green-700 dark:bg-green-900/20 dark:text-green-400" : "bg-red-50 text-red-700 dark:bg-red-900/20 dark:text-red-400"}`}>
                    {actionMessage.text}
                  </div>
                )}

                {/* Details grid */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-sm">
                  <DetailRow label={t('id')} value={selectedUser.id} mono />
                  <DetailRow label={t('username')} value={selectedUser.userName} />
                  <DetailRow label={t('email')} value={selectedUser.email} />
                  <DetailRow label={t('createdAt')} value={formatDate(selectedUser.createdAt)} />
                  <DetailRow label={t('updatedAt')} value={formatDate(selectedUser.updatedAt)} />
                  <DetailRow label={t('emailVerified')} value={bool(selectedUser.emailVerified)} />
                  <DetailRow label={t('twoFactorEnabled')} value={bool(selectedUser.twoFactorEnabled)} />
                  <DetailRow label={t('passkeyEnabled')} value={bool(selectedUser.passkeyEnabled)} />
                  <DetailRow label={t('status')} value={selectedUser.blocked ? t('blocked') : t('active')} />
                  <DetailRow label={t('failedLoginAttempts')} value={String(selectedUser.failedLoginAttempts ?? t('notAvailable'))} />
                  <DetailRow label={t('accountLockedUntil')} value={formatDate(selectedUser.accountLockedUntil)} />
                </div>

                {/* Roles */}
                <div>
                  <p className="text-xs font-semibold uppercase text-gray-500 dark:text-gray-400 mb-2">{t('roles')}</p>
                  <div className="flex flex-wrap gap-2 mb-3">
                    {(selectedUser.roles ?? []).map(role => (
                      <span key={role} className="flex items-center gap-1 px-2 py-1 text-xs rounded-full bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400">
                        {role}
                        <button
                          onClick={() => handleRemoveRole(selectedUser.id, role)}
                          className="hover:text-red-500 ml-1"
                          aria-label={t('removeRole')}
                        >
                          <IconX size={11} />
                        </button>
                      </span>
                    ))}
                  </div>
                  <div className="flex gap-2">
                    <input
                      type="text"
                      value={newRole}
                      onChange={e => setNewRole(e.target.value)}
                      onKeyDown={e => e.key === "Enter" && handleAddRole(selectedUser.id)}
                      placeholder={t('enterRole')}
                      className="flex-1 px-3 py-2 text-sm border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                    <button
                      onClick={() => handleAddRole(selectedUser.id)}
                      disabled={!newRole.trim()}
                      className="flex items-center gap-1 px-3 py-2 text-sm bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white rounded-lg transition-colors"
                    >
                      <IconPlus size={15} />
                      {t('addRole')}
                    </button>
                  </div>
                </div>

                {/* Actions */}
                <div className="flex flex-wrap gap-2 pt-2 border-t border-gray-200 dark:border-gray-700">
                  <button
                    onClick={() => handleBlock(selectedUser.id, !selectedUser.blocked)}
                    className={`flex items-center gap-2 px-4 py-2 text-sm rounded-lg transition-colors ${
                      selectedUser.blocked
                        ? "bg-green-600 hover:bg-green-700 text-white"
                        : "bg-red-600 hover:bg-red-700 text-white"
                    }`}
                  >
                    {selectedUser.blocked ? <IconLockOpen size={15} /> : <IconLock size={15} />}
                    {selectedUser.blocked ? t('unblockAccount') : t('blockAccount')}
                  </button>
                  <button
                    onClick={() => handleSendResetLink(selectedUser.id)}
                    className="flex items-center gap-2 px-4 py-2 text-sm bg-gray-100 hover:bg-gray-200 dark:bg-gray-700 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-200 rounded-lg transition-colors"
                  >
                    <IconMail size={15} />
                    {t('sendResetLink')}
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

function StatusBadge({ value, trueLabel, falseLabel }: { value?: boolean; trueLabel: string; falseLabel: string }) {
  if (value === undefined || value === null) return <span className="text-gray-400">—</span>;
  return value
    ? <span className="text-green-600 dark:text-green-400 font-medium">{trueLabel}</span>
    : <span className="text-gray-500 dark:text-gray-400">{falseLabel}</span>;
}

function ActionButton({ icon, label, onClick, danger }: { icon: React.ReactNode; label: string; onClick: () => void; danger?: boolean }) {
  return (
    <button
      onClick={onClick}
      title={label}
      className={`p-1.5 rounded transition-colors ${
        danger
          ? "text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20"
          : "text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700"
      }`}
    >
      {icon}
    </button>
  );
}

function DetailRow({ label, value, mono }: { label: string; value: string; mono?: boolean }) {
  return (
    <div>
      <p className="text-xs text-gray-500 dark:text-gray-400 mb-0.5">{label}</p>
      <p className={`text-gray-900 dark:text-white break-all ${mono ? "font-mono text-xs" : ""}`}>{value}</p>
    </div>
  );
}
