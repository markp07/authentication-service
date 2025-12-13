import type { Metadata } from "next";
import "./globals.css";
import { NextIntlClientProvider } from 'next-intl';
import { cookies, headers } from 'next/headers';
import { locales, defaultLocale, type Locale } from '../i18n/config';

export const metadata: Metadata = {
  title: "Authentication Demo",
  description: "A demonstration of modern authentication features including 2FA, passkeys, and secure user management.",
};

function getBrowserLocaleFromHeaders(headersList: Headers): Locale {
  const acceptLanguage = headersList.get('accept-language');
  
  if (acceptLanguage) {
    // Parse Accept-Language header (e.g., "en-US,en;q=0.9,nl;q=0.8")
    const languages = acceptLanguage
      .split(',')
      .map(lang => {
        const [code, q] = lang.trim().split(';q=');
        const quality = q ? parseFloat(q) : 1.0;
        return {
          code: code.split('-')[0].toLowerCase(),
          // Validate quality value, default to 0 if invalid
          quality: !isNaN(quality) && quality >= 0 && quality <= 1 ? quality : 0
        };
      })
      .sort((a, b) => b.quality - a.quality);
    
    // Find the first supported language
    for (const { code } of languages) {
      if (locales.includes(code as Locale)) {
        return code as Locale;
      }
    }
  }
  
  return defaultLocale;
}

async function getLocale(): Promise<Locale> {
  const cookieStore = await cookies();
  const cookieLocale = cookieStore.get('NEXT_LOCALE')?.value;
  
  // If user has selected a language (cookie exists), use that
  if (cookieLocale && locales.includes(cookieLocale as Locale)) {
    return cookieLocale as Locale;
  }
  
  // Otherwise, detect from browser
  const headersList = await headers();
  return getBrowserLocaleFromHeaders(headersList);
}

export default async function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const locale = await getLocale();
  
  let messages;
  try {
    messages = (await import(`../../messages/${locale}.json`)).default;
  } catch (error) {
    // Fallback to English if locale messages are not found
    console.warn(`Failed to load messages for locale '${locale}', falling back to English`, error);
    messages = (await import(`../../messages/en.json`)).default;
  }

  return (
    <html lang={locale} className="bg-gradient-to-br from-blue-100 via-white to-blue-300 dark:from-gray-900 dark:via-gray-800 dark:to-gray-700 min-h-screen">
      <body
        className="antialiased min-h-screen flex flex-col items-center justify-center text-gray-900 dark:text-gray-100 font-sans"
      >
        <NextIntlClientProvider locale={locale} messages={messages}>
          <div className="w-full max-w-full mx-auto flex flex-col min-h-screen">
            {children}
          </div>
        </NextIntlClientProvider>
      </body>
    </html>
  );
}
