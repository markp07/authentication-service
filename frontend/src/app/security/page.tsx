"use client";

import React from "react";
import { useRouter } from "next/navigation";
import SecurityPage from "../../components/SecurityPage";
import Sidebar from "../../components/Sidebar";
import Modal from "../../components/Modal";
import Setup2FA from "../../components/Setup2FA";
import ChangePassword from "../../components/ChangePassword";
import { AUTH_API_BASE } from "../../utils/api";

export default function Security() {
  const router = useRouter();
  const [modal, setModal] = React.useState<"2fa" | "changePassword" | null>(null);
  const [loggedIn, setLoggedIn] = React.useState(false);
  const [username, setUsername] = React.useState<string | null>(null);
  const [checkingLogin, setCheckingLogin] = React.useState(true);

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
        } else {
          router.push("/login?callback=" + encodeURIComponent("/security"));
        }
      } catch {
        setLoggedIn(false);
        router.push("/login?callback=" + encodeURIComponent("/security"));
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

  function handleNavigate(page: "profile" | "security") {
    if (page === "profile") {
      router.push("/");
    } else if (page === "security") {
      router.push("/security");
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

  return (
    <div className="flex min-h-screen w-full bg-gray-50 dark:bg-gray-900">
      <Sidebar
        username={username}
        activePage="security"
        onNavigate={handleNavigate}
        onLogout={handleLogout}
      />
      
      <main className="flex-1 overflow-auto lg:ml-64">
        <SecurityPage
          onChangePassword={() => setModal("changePassword")}
          onToggle2FA={() => setModal("2fa")}
        />
      </main>

      <Modal open={modal === "2fa"} onClose={() => setModal(null)}>
        <Setup2FA />
      </Modal>
      <Modal open={modal === "changePassword"} onClose={() => setModal(null)}>
        <ChangePassword onClose={() => setModal(null)} />
      </Modal>
    </div>
  );
}
