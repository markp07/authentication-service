// Simple manual mock for next-intl used in tests
const React = require('react');
const path = require('path');

// Load default English messages for simple translation lookup in tests
let messages = {};
try {
  // Use process.cwd() so Jest's module resolution doesn't affect finding the messages folder
  const messagesPath = path.join(process.cwd(), 'messages', 'en.json');
  messages = require(messagesPath);
} catch (e) {
  // fallback to empty
  messages = {};
}

function interpolate(template, vars) {
  if (!template) return template;
  return template.replace(/\{([^}]+)\}/g, (_, name) => {
    if (!vars) return '';
    return vars[name] ?? '';
  });
}

module.exports = {
  useTranslations: (namespace) => (key, vars) => {
    const ns = messages[namespace] || {};
    const value = ns[key];
    if (typeof value === 'string') return interpolate(value, vars);
    return key;
  },
  useFormatter: () => (v => String(v)),
  useLocale: () => 'en',
  useMessages: () => ({}),
  useNow: () => new Date(),
  useTimeZone: () => 'UTC',
  NextIntlClientProvider: ({children}) => React.createElement(React.Fragment, null, children),
  IntlProvider: ({children}) => React.createElement(React.Fragment, null, children),
};
