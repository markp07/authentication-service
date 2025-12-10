const nextJest = require('next/jest');

const createJestConfig = nextJest({
  dir: './',
});

const customJestConfig = {
  setupFilesAfterEnv: ['<rootDir>/jest.setup.ts'],
  testEnvironment: 'jest-environment-jsdom',
  moduleNameMapper: {
    '^@/(.*)$': '<rootDir>/src/$1',
    // Map next-intl (and any subpath) to a manual mock so ESM release doesn't break Jest.
    '^next-intl(/.*)?$': '<rootDir>/test/__mocks__/next-intl.js',
  },
  testPathIgnorePatterns: ['<rootDir>/.next/'],
  // Transform ESM packages in node_modules that export native ESM (like next-intl)
  // by allowing Jest to transform them instead of ignoring them.
  transformIgnorePatterns: ['/node_modules/(?!(next-intl)/)'],
};

module.exports = createJestConfig(customJestConfig);
