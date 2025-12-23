import type { NextConfig } from "next";
import packageJson from "./package.json";

const nextConfig: NextConfig = {
  env: {
    NEXT_PUBLIC_BUILD_TIME: new Date().toISOString(),
    NEXT_PUBLIC_APP_VERSION: packageJson.version,
  },
};

// Only load next-intl plugin when not in test environment to avoid @parcel/watcher issues in CI
if (process.env.NODE_ENV !== 'test') {
  const createNextIntlPlugin = require('next-intl/plugin');
  const withNextIntl = createNextIntlPlugin('./src/i18n/request.ts');
  module.exports = withNextIntl(nextConfig);
} else {
  module.exports = nextConfig;
}
