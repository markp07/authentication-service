'use client';

import React, { useState, useEffect } from 'react';
import { useTranslations } from 'next-intl';
import { getLocale, setLocale, locales, type Locale } from '../i18n/client';
import { IconWorld, IconChevronDown } from '@tabler/icons-react';

export default function PublicLanguageSelector() {
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

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Escape') {
      setIsOpen(false);
    }
  };

  return (
    <div className="fixed top-4 right-4 z-50">
      <div className="relative">
        <button
          id="language-selector-button"
          onClick={() => setIsOpen(!isOpen)}
          onKeyDown={handleKeyDown}
          aria-expanded={isOpen}
          aria-haspopup="true"
          className="flex items-center gap-2 px-3 py-2 rounded-lg bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 shadow-lg border border-gray-200 dark:border-gray-700 transition-all duration-200"
          aria-label="Select language"
        >
          <IconWorld size={20} />
          <span className="font-medium text-sm">{t(currentLocale)}</span>
          <IconChevronDown size={16} className={`transition-transform duration-200 ${isOpen ? 'rotate-180' : ''}`} />
        </button>

        {isOpen && (
          <>
            {/* Backdrop */}
            <div
              className="fixed inset-0 z-40"
              onClick={() => setIsOpen(false)}
              onKeyDown={handleKeyDown}
            />
            
            {/* Dropdown */}
            <div 
              role="menu"
              aria-labelledby="language-selector-button"
              className="absolute top-full right-0 mt-2 bg-white dark:bg-gray-800 rounded-lg shadow-xl border border-gray-200 dark:border-gray-700 overflow-hidden z-50 min-w-[140px]"
            >
              {locales.map((locale) => (
                <button
                  key={locale}
                  role="menuitem"
                  onClick={() => handleLocaleChange(locale)}
                  onKeyDown={handleKeyDown}
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
    </div>
  );
}
