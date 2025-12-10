'use client';

import React from "react";
import { IconHome, IconUser, IconShield, IconLogout, IconMenu2, IconX } from "@tabler/icons-react";
import { useTranslations } from 'next-intl';
import LanguageSelector from './LanguageSelector';

interface SidebarProps {
  username: string | null;
  activePage: "dashboard" | "profile" | "security";
  onNavigate: (page: "dashboard" | "profile" | "security") => void;
  onLogout: () => void;
}

export default function Sidebar({ username, activePage, onNavigate, onLogout }: SidebarProps) {
  const t = useTranslations('sidebar');
  const [isOpen, setIsOpen] = React.useState(false);

  const menuItems = [
    { id: "dashboard" as const, label: t('dashboard'), icon: IconHome },
    { id: "profile" as const, label: t('profile'), icon: IconUser },
    { id: "security" as const, label: t('security'), icon: IconShield },
  ];

  return (
    <>
      {/* Mobile menu button */}
      <button
        className="lg:hidden fixed top-3 right-3 z-50 bg-white dark:bg-gray-800 p-2 rounded-lg shadow-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
        onClick={() => setIsOpen(!isOpen)}
        aria-label="Toggle menu"
      >
        {isOpen ? <IconX size={24} /> : <IconMenu2 size={24} />}
      </button>

      {/* Overlay for mobile */}
      {isOpen && (
        <div
          className="lg:hidden fixed inset-0 bg-transparent z-40"
          onClick={() => setIsOpen(false)}
        />
      )}

      {/* Sidebar */}
      <aside
        className={`fixed inset-y-0 left-0 z-40 w-64 bg-white dark:bg-gray-800 shadow-xl transform transition-transform duration-300 ease-in-out lg:transform-none lg:h-screen ${
          isOpen ? "translate-x-0" : "-translate-x-full lg:translate-x-0"
        }`}
      >
        <div className="flex flex-col h-full lg:h-screen">
          {/* Header */}
          <div className="p-6 border-b border-gray-200 dark:border-gray-700">
            <h1 className="text-2xl font-bold text-blue-600 dark:text-blue-400">Weather</h1>
            <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">{t('welcome', { username: username || 'User' })}</p>
            <p className="text-xs text-gray-500 dark:text-gray-500 mt-1">
              v{process.env.NEXT_PUBLIC_APP_VERSION || '0.0.0'} • {new Date(process.env.NEXT_PUBLIC_BUILD_TIME || Date.now()).toLocaleString()}
            </p>
          </div>

          {/* Navigation */}
          <nav className="flex-1 p-4 space-y-2">
            {menuItems.map((item) => {
              const Icon = item.icon;
              const isActive = activePage === item.id;
              return (
                <button
                  key={item.id}
                  onClick={() => {
                    onNavigate(item.id);
                    setIsOpen(false);
                  }}
                  className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg transition-all duration-200 ${
                    isActive
                      ? "bg-blue-600 text-white shadow-md"
                      : "text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700"
                  }`}
                >
                  <Icon size={20} />
                  <span className="font-medium">{item.label}</span>
                </button>
              );
            })}
          </nav>

          {/* Footer */}
          <div className="p-4 border-t border-gray-200 dark:border-gray-700 space-y-2">
            <LanguageSelector />
            <button
              onClick={onLogout}
              className="w-full flex items-center gap-3 px-4 py-3 rounded-lg text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 transition-all duration-200"
            >
              <IconLogout size={20} />
              <span className="font-medium">{t('logout')}</span>
            </button>
          </div>
        </div>
      </aside>
    </>
  );
}
