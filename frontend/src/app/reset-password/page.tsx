"use client";

import React, { useEffect, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { useTranslations } from 'next-intl';
import ResetPassword from "../../components/ResetPassword";
import PublicLanguageSelector from "../../components/PublicLanguageSelector";
import { isValidCallback, getSafeCallback } from "../../utils/callbackValidation";

const isDev = typeof window !== "undefined" && window.location.hostname === "localhost";
const AUTH_API_BASE = isDev
  ? (process.env.NEXT_PUBLIC_API_URL || "http://localhost:12002")
  : "https://auth.markpost.dev";

function ResetPasswordPageContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const t = useTranslations('pageTitle');
  const [checkingAuth, setCheckingAuth] = React.useState(true);

  // Update document title based on selected language
  useEffect(() => {
    document.title = t('resetPassword');
  }, [t]);

  useEffect(() => {
    // Check if already logged in
    async function checkLogin() {
      try {
        const res = await fetch(`${AUTH_API_BASE}/api/auth/v1/user`, { credentials: "include" });
        if (res.ok) {
          // Already logged in, redirect to callback or home
          const callback = searchParams.get("callback");
          router.push(getSafeCallback(callback));
          return;
        }
      } catch {
        // Not logged in, continue to show reset password page
      }
      setCheckingAuth(false);
    }
    checkLogin();
  }, [router, searchParams]);

  const handleBack = () => {
    const callback = searchParams.get("callback");
    if (isValidCallback(callback)) {
      router.push(`/forgot-password?callback=${encodeURIComponent(callback!)}`);
    } else {
      router.push("/forgot-password");
    }
  };

  const handleLogin = () => {
    const callback = searchParams.get("callback");
    if (isValidCallback(callback)) {
      router.push(`/login?callback=${encodeURIComponent(callback!)}`);
    } else {
      router.push("/login");
    }
  };

  if (checkingAuth) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen w-full bg-gray-50 dark:bg-gray-900">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500 mb-4"></div>
        <div className="text-lg font-semibold text-blue-700 dark:text-blue-400">Loading...</div>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center justify-center min-h-screen w-full bg-gray-50 dark:bg-gray-900 p-4">
      <PublicLanguageSelector />
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-2xl p-6 min-w-[320px] w-full max-w-md">
        <ResetPassword
          onBack={handleBack}
          onLogin={handleLogin}
        />
      </div>
    </div>
  );
}

export default function ResetPasswordPage() {
  return (
    <Suspense fallback={
      <div className="flex flex-col items-center justify-center min-h-screen w-full bg-gray-50 dark:bg-gray-900">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500 mb-4"></div>
        <div className="text-lg font-semibold text-blue-700 dark:text-blue-400">Loading...</div>
      </div>
    }>
      <ResetPasswordPageContent />
    </Suspense>
  );
}
