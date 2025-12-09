'use client';

import { locales, defaultLocale, type Locale } from './config';

export { locales, defaultLocale, type Locale };

export function getLocale(): Locale {
  if (typeof window === 'undefined') return defaultLocale;
  
  const cookieValue = document.cookie
    .split('; ')
    .find(row => row.startsWith('NEXT_LOCALE='))
    ?.split('=')[1];
    
  return (locales.includes(cookieValue as Locale) ? cookieValue : defaultLocale) as Locale;
}

export function setLocale(locale: Locale): void {
  if (typeof window === 'undefined') return;
  
  // Set cookie for 1 year
  const expires = new Date();
  expires.setFullYear(expires.getFullYear() + 1);
  document.cookie = `NEXT_LOCALE=${locale}; expires=${expires.toUTCString()}; path=/; SameSite=Lax`;
  
  // Reload to apply the new locale
  window.location.reload();
}
