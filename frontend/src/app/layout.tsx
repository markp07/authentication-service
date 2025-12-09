import type { Metadata } from "next";
import "./globals.css";
import { NextIntlClientProvider } from 'next-intl';
import { cookies } from 'next/headers';
import { locales, defaultLocale, type Locale } from '../i18n/config';

export const metadata: Metadata = {
  title: "Weather",
  description: "A modern weather app with authentication and user profile.",
};

async function getLocale(): Promise<Locale> {
  const cookieStore = await cookies();
  const locale = cookieStore.get('NEXT_LOCALE')?.value;
  return (locales.includes(locale as Locale) ? locale : defaultLocale) as Locale;
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
