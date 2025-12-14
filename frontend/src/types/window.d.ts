// Type definitions for runtime environment variables injected via window.__ENV__

interface RuntimeEnv {
  NEXT_PUBLIC_API_URL?: string;
  NEXT_PUBLIC_BASE_DOMAIN?: string;
  NEXT_PUBLIC_SUPPORT_EMAIL?: string;
}

interface Window {
  __ENV__?: RuntimeEnv;
}

