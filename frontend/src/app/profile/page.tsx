"use client";

import React from "react";
import { useRouter } from "next/navigation";
import ProfilePage from "../../components/ProfilePage";
import Sidebar from "../../components/Sidebar";
import Modal from "../../components/Modal";
import DeleteAccountModal from "../../components/DeleteAccountModal";

const isDev = typeof window !== "undefined" && window.location.hostname === "localhost";
const AUTH_API_BASE = isDev
  ? (process.env.NEXT_PUBLIC_API_URL || "http://localhost:12002")
  : "https://demo.markpost.dev";

export default function Profile() {
  const router = useRouter();
  const [modal, setModal] = React.useState<"deleteAccount" | null>(null);
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
          router.push("/");
        }
      } catch {
        setLoggedIn(false);
        router.push("/");
      }
      setCheckingLogin(false);
    }
    checkLogin();
  }, [router]);

  async function handleLogout() {
    await fetch(`${AUTH_API_BASE}/api/auth/v1/logout`, { method: "POST", credentials: "include" });
    setLoggedIn(false);
    router.push("/");
  }

  function handleNavigate(page: "dashboard" | "profile" | "security") {
    if (page === "dashboard") {
      router.push("/");
    } else if (page === "profile") {
      router.push("/profile");
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
        activePage="profile"
        onNavigate={handleNavigate}
        onLogout={handleLogout}
      />
      
      <main className="flex-1 overflow-auto lg:ml-64">
        <ProfilePage
          onSecurity={() => router.push("/security")}
          onDeleteAccount={() => setModal("deleteAccount")}
        />
      </main>

      <Modal open={modal === "deleteAccount"} onClose={() => setModal(null)}>
        <DeleteAccountModal
          onSuccess={handleLogout}
          onCancel={() => setModal(null)}
        />
      </Modal>
    </div>
  );
}
