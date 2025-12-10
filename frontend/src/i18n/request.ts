import { getRequestConfig } from 'next-intl/server';
import { cookies } from 'next/headers';
import { locales, defaultLocale, type Locale } from './config';

export { locales, defaultLocale, type Locale };

async function getLocaleFromCookies(): Promise<Locale> {
  const cookieStore = await cookies();
  const locale = cookieStore.get('NEXT_LOCALE')?.value;
  return (locales.includes(locale as Locale) ? locale : defaultLocale) as Locale;
}

export default getRequestConfig(async () => {
  const locale = await getLocaleFromCookies();

  return {
    locale,
    messages: (await import(`../../messages/${locale}.json`)).default
  };
});
