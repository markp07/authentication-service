'use client';

import { locales, defaultLocale, type Locale } from './config';

export { locales, defaultLocale, type Locale };

function getBrowserLocale(): Locale {
  if (typeof window === 'undefined') return defaultLocale;
  
  // Get browser language
  const browserLang = navigator.language || (navigator as any).userLanguage;
  
  // Extract the language code (e.g., 'en-US' -> 'en', 'nl-NL' -> 'nl')
  const langCode = browserLang.split('-')[0].toLowerCase();
  
  // Check if the browser language is supported
  if (locales.includes(langCode as Locale)) {
    return langCode as Locale;
  }
  
  return defaultLocale;
}

export function getLocale(): Locale {
  if (typeof window === 'undefined') return defaultLocale;
  
  // First check for stored preference in cookie
  const cookieValue = document.cookie
    .split('; ')
    .find(row => row.startsWith('NEXT_LOCALE='))
    ?.split('=')[1];
  
  // If cookie exists and is valid, use it (user preference overrides browser default)
  if (cookieValue && locales.includes(cookieValue as Locale)) {
    return cookieValue as Locale;
  }
  
  // Otherwise, detect from browser language
  return getBrowserLocale();
}

export function setLocale(locale: Locale): void {
  if (typeof window === 'undefined') return;
  
  // Set cookie for 1 year
  const expires = new Date();
  expires.setFullYear(expires.getFullYear() + 1);
  document.cookie = `NEXT_LOCALE=${locale}; expires=${expires.toUTCString()}; path=/; SameSite=Lax`;
  
  // Full page reload is required because next-intl loads messages server-side
  // The locale needs to be available during SSR for proper hydration
  window.location.reload();
}
