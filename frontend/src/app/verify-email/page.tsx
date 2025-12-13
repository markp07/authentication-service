"use client";

import React, { useEffect, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { IconMailCheck, IconMailX, IconLoader2 } from "@tabler/icons-react";
import { AUTH_API_BASE } from "../../utils/api";

function VerifyEmailContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [status, setStatus] = React.useState<"loading" | "success" | "error" | "manual">("loading");
  const [errorMessage, setErrorMessage] = React.useState<string | null>(null);
  const [manualToken, setManualToken] = React.useState("");
  const [verifying, setVerifying] = React.useState(false);

  useEffect(() => {
    const token = searchParams.get("token");
    if (token) {
      verifyToken(token);
    } else {
      // No token in URL, show manual input form
      setStatus("manual");
    }
  }, [searchParams]);

  async function verifyToken(token: string) {
    setStatus("loading");
    setErrorMessage(null);
    try {
      const res = await fetch(`${AUTH_API_BASE}/api/auth/v1/email/verify?token=${encodeURIComponent(token)}`, {
        method: "GET",
        credentials: "include",
      });
      if (res.ok) {
        setStatus("success");
      } else {
        const data = await res.json().catch(() => null);
        setErrorMessage(data?.description || "Verification failed. The token may be invalid or expired.");
        setStatus("error");
      }
    } catch {
      setErrorMessage("Network error. Please try again.");
      setStatus("error");
    }
  }

  async function handleManualVerify(e: React.FormEvent) {
    e.preventDefault();
    if (!manualToken.trim()) {
      setErrorMessage("Please enter a verification token.");
      return;
    }
    setVerifying(true);
    setErrorMessage(null);
    await verifyToken(manualToken.trim());
    setVerifying(false);
  }

  function handleGoToLogin() {
    router.push("/login");
  }

  function handleGoToProfile() {
    router.push("/");
  }

  function handleTryAgain() {
    setStatus("manual");
    setErrorMessage(null);
  }

  return (
    <div className="flex flex-col items-center justify-center min-h-screen w-full bg-gray-50 dark:bg-gray-900 p-4">
      <div className="bg-white dark:bg-gray-800 rounded-xl shadow-2xl p-8 min-w-[320px] w-full max-w-md">
        {status === "loading" && (
          <div className="flex flex-col items-center gap-4">
            <IconLoader2 size={64} className="text-blue-500 animate-spin" />
            <h2 className="text-xl font-bold text-gray-900 dark:text-white">Verifying your email...</h2>
            <p className="text-gray-600 dark:text-gray-400 text-center">Please wait while we verify your email address.</p>
          </div>
        )}

        {status === "success" && (
          <div className="flex flex-col items-center gap-4">
            <div className="w-20 h-20 bg-green-100 dark:bg-green-900/30 rounded-full flex items-center justify-center">
              <IconMailCheck size={48} className="text-green-600 dark:text-green-400" />
            </div>
            <h2 className="text-2xl font-bold text-gray-900 dark:text-white">Email Verified!</h2>
            <p className="text-gray-600 dark:text-gray-400 text-center">Your email address has been successfully verified. You can now enjoy full access to your account.</p>
            <div className="flex flex-col gap-2 w-full mt-4">
              <button
                onClick={handleGoToProfile}
                className="w-full px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium transition-colors"
              >
                Go to Profile
              </button>
              <button
                onClick={handleGoToLogin}
                className="w-full px-4 py-2 bg-gray-200 hover:bg-gray-300 dark:bg-gray-700 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300 rounded-lg font-medium transition-colors"
              >
                Go to Login
              </button>
            </div>
          </div>
        )}

        {status === "error" && (
          <div className="flex flex-col items-center gap-4">
            <div className="w-20 h-20 bg-red-100 dark:bg-red-900/30 rounded-full flex items-center justify-center">
              <IconMailX size={48} className="text-red-600 dark:text-red-400" />
            </div>
            <h2 className="text-2xl font-bold text-gray-900 dark:text-white">Verification Failed</h2>
            <p className="text-red-600 dark:text-red-400 text-center">{errorMessage}</p>
            <div className="flex flex-col gap-2 w-full mt-4">
              <button
                onClick={handleTryAgain}
                className="w-full px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium transition-colors"
              >
                Enter Token Manually
              </button>
              <button
                onClick={handleGoToProfile}
                className="w-full px-4 py-2 bg-gray-200 hover:bg-gray-300 dark:bg-gray-700 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-300 rounded-lg font-medium transition-colors"
              >
                Go to Profile
              </button>
            </div>
          </div>
        )}

        {status === "manual" && (
          <form onSubmit={handleManualVerify} className="flex flex-col gap-4">
            <div className="flex flex-col items-center gap-2 mb-2">
              <div className="w-16 h-16 bg-blue-100 dark:bg-blue-900/30 rounded-full flex items-center justify-center">
                <IconMailCheck size={32} className="text-blue-600 dark:text-blue-400" />
              </div>
              <h2 className="text-2xl font-bold text-gray-900 dark:text-white">Verify Email</h2>
              <p className="text-gray-600 dark:text-gray-400 text-center text-sm">
                Enter the verification token from your email to verify your email address.
              </p>
            </div>
            {errorMessage && (
              <div className="text-red-600 dark:text-red-400 text-sm bg-red-100 dark:bg-red-900/20 rounded-lg px-3 py-2">
                {errorMessage}
              </div>
            )}
            <input
              type="text"
              placeholder="Verification Token"
              className="w-full px-4 py-3 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-white dark:bg-gray-700 text-gray-900 dark:text-white"
              required
              value={manualToken}
              onChange={e => setManualToken(e.target.value)}
            />
            <button
              type="submit"
              className="w-full px-4 py-3 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium transition-colors disabled:opacity-50"
              disabled={verifying}
            >
              {verifying ? "Verifying..." : "Verify Email"}
            </button>
            <button
              type="button"
              onClick={handleGoToProfile}
              className="w-full px-4 py-2 text-blue-600 dark:text-blue-400 hover:underline font-medium"
            >
              Back to Profile
            </button>
          </form>
        )}
      </div>
    </div>
  );
}

export default function VerifyEmailPage() {
  return (
    <Suspense fallback={
      <div className="flex flex-col items-center justify-center min-h-screen w-full bg-gray-50 dark:bg-gray-900">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500 mb-4"></div>
        <div className="text-lg font-semibold text-blue-700 dark:text-blue-400">Loading...</div>
      </div>
    }>
      <VerifyEmailContent />
    </Suspense>
  );
}
