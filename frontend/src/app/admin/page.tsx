"use client";

import React from "react";
import { useRouter } from "next/navigation";
import { useTranslations } from 'next-intl';
import AdminPanel from "../../components/AdminPanel";
import Sidebar from "../../components/Sidebar";
import { AUTH_API_BASE } from "../../utils/api";

export default function AdminPage() {
  const router = useRouter();
  const t = useTranslations('pageTitle');
  const tAdmin = useTranslations('admin');
  const [loggedIn, setLoggedIn] = React.useState(false);
  const [username, setUsername] = React.useState<string | null>(null);
  const [isAdmin, setIsAdmin] = React.useState(false);
  const [checkingLogin, setCheckingLogin] = React.useState(true);

  React.useEffect(() => {
    document.title = t('admin');
  }, [t]);

  React.useEffect(() => {
    async function checkLogin() {
      try {
        let res = await fetch(`${AUTH_API_BASE}/api/auth/v1/user`, { credentials: "include" });
        if (res.status === 401) {
          const refreshRes = await fetch(`${AUTH_API_BASE}/api/auth/v1/refresh`, { method: "POST", credentials: "include" });
          if (refreshRes.ok) {
            res = await fetch(`${AUTH_API_BASE}/api/auth/v1/user`, { credentials: "include" });
          }
        }
        setLoggedIn(res.ok);
        if (res.ok) {
          const data = await res.json();
          setUsername(data.userName || null);
          const admin = Array.isArray(data.roles) && data.roles.includes("ADMIN");
          setIsAdmin(admin);
          if (!admin) {
            // Authenticated but not admin — stay on page to show access denied
          }
        } else {
          router.push("/login?callback=" + encodeURIComponent("/admin"));
        }
      } catch {
        router.push("/login?callback=" + encodeURIComponent("/admin"));
      }
      setCheckingLogin(false);
    }
    checkLogin();
  }, [router]);

  async function handleLogout() {
    await fetch(`${AUTH_API_BASE}/api/auth/v1/logout`, { method: "POST", credentials: "include" });
    setLoggedIn(false);
    router.push("/login");
  }

  function handleNavigate(page: "profile" | "security" | "admin") {
    if (page === "profile") {
      router.push("/");
    } else if (page === "security") {
      router.push("/security");
    } else if (page === "admin") {
      router.push("/admin");
    }
  }

  if (checkingLogin) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen w-full">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500 mb-4"></div>
        <div className="text-lg font-semibold text-blue-700">Loading...</div>
      </div>
    );
  }

  if (!loggedIn) {
    return null;
  }

  if (!isAdmin) {
    return (
      <div className="flex min-h-screen w-full bg-gray-50 dark:bg-gray-900">
        <Sidebar
          username={username}
          activePage="admin"
          onNavigate={handleNavigate}
          onLogout={handleLogout}
          isAdmin={false}
        />
        <main className="flex-1 overflow-auto lg:ml-64 flex items-center justify-center">
          <div className="text-center p-8">
            <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">{tAdmin('accessDenied')}</h2>
            <p className="text-gray-600 dark:text-gray-400">{tAdmin('noPermission')}</p>
          </div>
        </main>
      </div>
    );
  }

  return (
    <div className="flex min-h-screen w-full bg-gray-50 dark:bg-gray-900">
      <Sidebar
        username={username}
        activePage="admin"
        onNavigate={handleNavigate}
        onLogout={handleLogout}
        isAdmin={isAdmin}
      />
      <main className="flex-1 overflow-auto lg:ml-64">
        <AdminPanel />
      </main>
    </div>
  );
}
