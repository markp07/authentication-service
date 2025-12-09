'use client';

import React, { useState, useEffect } from 'react';
import { useTranslations } from 'next-intl';
import { getLocale, setLocale, locales, type Locale } from '../i18n/client';
import { IconWorld, IconChevronDown } from '@tabler/icons-react';

export default function LanguageSelector() {
  const t = useTranslations('languages');
  const [currentLocale, setCurrentLocale] = useState<Locale>('en');
  const [isOpen, setIsOpen] = useState(false);

  // Get locale from cookie on mount
  useEffect(() => {
    setCurrentLocale(getLocale());
  }, []);

  const handleLocaleChange = (locale: Locale) => {
    setCurrentLocale(locale);
    setLocale(locale);
    setIsOpen(false);
  };

  return (
    <div className="relative">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="w-full flex items-center gap-3 px-4 py-3 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 transition-all duration-200"
        aria-label="Select language"
      >
        <IconWorld size={20} />
        <span className="flex-1 font-medium text-left">{t(currentLocale)}</span>
        <IconChevronDown size={16} className={`transition-transform duration-200 ${isOpen ? 'rotate-180' : ''}`} />
      </button>

      {isOpen && (
        <>
          {/* Backdrop */}
          <div
            className="fixed inset-0 z-40"
            onClick={() => setIsOpen(false)}
          />
          
          {/* Dropdown */}
          <div className="absolute bottom-full left-0 right-0 mb-2 bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 overflow-hidden z-50">
            {locales.map((locale) => (
              <button
                key={locale}
                onClick={() => handleLocaleChange(locale)}
                className={`w-full px-4 py-2 text-left hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors ${
                  currentLocale === locale
                    ? 'bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400 font-medium'
                    : 'text-gray-700 dark:text-gray-300'
                }`}
              >
                {t(locale)}
              </button>
            ))}
          </div>
        </>
      )}
    </div>
  );
}
