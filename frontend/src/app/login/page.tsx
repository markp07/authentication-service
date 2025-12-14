"use client";

import React, { useEffect, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useTranslations } from 'next-intl';
import Login from "../../components/Login";
import PublicLanguageSelector from "../../components/PublicLanguageSelector";
import { validateAuthToken } from "../../utils/retry";
import { isValidCallback, getSafeCallback } from "../../utils/callbackValidation";

const isDev = typeof window !== "undefined" && window.location.hostname === "localhost";
const AUTH_API_BASE = isDev ? "http://localhost:12002" : (process.env.NEXT_PUBLIC_API_URL || "https://auth.markpost.dev");

function LoginPageContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const t = useTranslations('pageTitle');
  const [checkingAuth, setCheckingAuth] = React.useState(true);

  // Update document title based on selected language
  useEffect(() => {
    document.title = t('login');
  }, [t]);

  useEffect(() => {
    // Check if already logged in, especially when there's a callback URL
    async function checkLogin() {
      const callback = searchParams.get("callback");
      
      // If there's a callback URL, validate token with retry mechanism
      if (callback) {
        // Validate callback URL to prevent open redirect attacks
        if (!isValidCallback(callback)) {
          setCheckingAuth(false);
          return;
        }
        
        const isValid = await validateAuthToken(AUTH_API_BASE);
        if (isValid) {
          // Token is valid or was refreshed successfully, redirect to callback
          router.push(callback);
          return;
        }
        // Token is invalid and refresh failed, show login screen
        setCheckingAuth(false);
        return;
      }
      
      // No callback URL, do simple check without retry
      try {
        const res = await fetch(`${AUTH_API_BASE}/api/auth/v1/user`, { credentials: "include" });
        if (res.ok) {
          // Already logged in, redirect to profile
          router.push("/");
          return;
        }
      } catch {
        // Not logged in, continue to show login page
      }
      setCheckingAuth(false);
    }
    checkLogin();
  }, [router, searchParams]);

  const handleSuccess = () => {
    const callback = searchParams.get("callback");
    // Validate callback URL to prevent open redirect attacks
    router.push(getSafeCallback(callback));
  };

  const handleRegister = () => {
    const callback = searchParams.get("callback");
    // Only pass callback if it's valid
    if (isValidCallback(callback)) {
      router.push(`/register?callback=${encodeURIComponent(callback!)}`);
    } else {
      router.push("/register");
    }
  };

  const handleForgot = () => {
    const callback = searchParams.get("callback");
    // Only pass callback if it's valid
    if (isValidCallback(callback)) {
      router.push(`/forgot-password?callback=${encodeURIComponent(callback!)}`);
    } else {
      router.push("/forgot-password");
    }
  };

  if (checkingAuth) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen w-full bg-gray-50 dark:bg-gray-900">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500 mb-4"></div>
        <div className="text-lg font-semibold text-blue-700 dark:text-blue-400">Authenticating...</div>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center justify-center min-h-screen w-full bg-gray-50 dark:bg-gray-900 p-4">
      <PublicLanguageSelector />
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-2xl p-6 min-w-[320px] w-full max-w-md">
        <Login
          onSuccess={handleSuccess}
          onRegister={handleRegister}
          onForgot={handleForgot}
        />
      </div>
    </div>
  );
}

export default function LoginPage() {
  return (
    <Suspense fallback={
      <div className="flex flex-col items-center justify-center min-h-screen w-full bg-gray-50 dark:bg-gray-900">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500 mb-4"></div>
        <div className="text-lg font-semibold text-blue-700 dark:text-blue-400">Authenticating...</div>
      </div>
    }>
      <LoginPageContent />
    </Suspense>
  );
}
