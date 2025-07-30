"use client";

import React from "react";
import Modal from "../components/Modal";
import Login from "../components/Login";
import Register from "../components/Register";
import ForgotPassword from "../components/ForgotPassword";
import ResetPassword from "../components/ResetPassword";
import Profile from "../components/Profile";
import Setup2FA from "../components/Setup2FA";
import ChangePassword from "../components/ChangePassword";
import { IconTrash, IconShieldLock, IconSun, IconLogout, IconUser } from "@tabler/icons-react";
import type { Weather } from "../types/Weather";
import { weatherCodeMap } from "../types/WeatherCodeMap";

const API_BASE = "http://localhost:12002/v1";

function getWeatherIcon(code: string, size = 32) {
  return weatherCodeMap[code]?.icon(size) || <IconSun size={size} />;
}
function getWeatherLabel(code: string) {
  return weatherCodeMap[code]?.label || code;
}

interface UserMenuProps {
  username: string | null;
  onProfile: () => void;
  on2FA: () => void;
  onChangePassword: () => void;
  onDelete: () => void;
  onLogout: () => void;
}

function UserMenu({ username, onProfile, on2FA, onChangePassword, onDelete, onLogout }: UserMenuProps) {
  const [open, setOpen] = React.useState(false);
  return (
    <div className="fixed top-1 right-1 z-50">
      <button
        className="flex items-center gap-2 bg-blue-600 text-white rounded-full px-3 py-1 shadow-lg hover:bg-blue-700 focus:outline-none"
        onClick={() => setOpen((v) => !v)}
        aria-label="User menu"
      >
        <IconUser size={18} />
        <span className="text-sm font-semibold">{username || "User"}</span>
      </button>
      {open && (
        <div className="absolute right-0 mt-2 w-48 bg-white dark:bg-gray-900 rounded-lg shadow-lg border border-gray-200 dark:border-gray-800 flex flex-col">
          <button className="px-4 py-2 text-left hover:bg-blue-50 dark:hover:bg-gray-800" onClick={onProfile}><IconUser size={18} className="inline mr-2" />Profile</button>
          <button className="px-4 py-2 text-left hover:bg-blue-50 dark:hover:bg-gray-800" onClick={onChangePassword}><IconShieldLock size={18} className="inline mr-2" />Change Password</button>
          <button className="px-4 py-2 text-left hover:bg-blue-50 dark:hover:bg-gray-800" onClick={on2FA}><IconShieldLock size={18} className="inline mr-2" />Enable 2FA</button>
          <button className="px-4 py-2 text-left hover:bg-blue-50 dark:hover:bg-gray-800" onClick={onDelete}><IconTrash size={18} className="inline mr-2" />Delete Account</button>
          <button className="px-4 py-2 text-left hover:bg-blue-50 dark:hover:bg-gray-800" onClick={onLogout}><IconLogout size={18} className="inline mr-2" />Logout</button>
        </div>
      )}
    </div>
  );
}

export default function Home() {
  const [modal, setModal] = React.useState<
    | "login"
    | "register"
    | "forgot"
    | "reset"
    | "profile"
    | "2fa"
    | "changePassword"
    | "deleteAccount"
    | null
  >("login");
  const [globalMessage, setGlobalMessage] = React.useState<string | null>(null);
  const [showWeather, setShowWeather] = React.useState(false);
  const [weatherError, setWeatherError] = React.useState<string | null>(null);
  const [weather, setWeather] = React.useState<Weather | null>(null);
  const [loggedIn, setLoggedIn] = React.useState(false);
  const [username, setUsername] = React.useState<string | null>(null);
  const [checkingLogin, setCheckingLogin] = React.useState(true);

  // Modal open/close helpers
  const openModal = (name: typeof modal) => setModal(name);
  const closeModal = () => setModal(null);

  React.useEffect(() => {
    async function checkLogin() {
      try {
        let res = await fetch(`${API_BASE}/user`, { credentials: "include" });
        if (res.status === 401) {
          // Try refresh token
          const refreshRes = await fetch(`${API_BASE}/auth/refresh`, { method: "POST", credentials: "include" });
          if (refreshRes.ok) {
            res = await fetch(`${API_BASE}/user`, { credentials: "include" });
          }
        }
        setLoggedIn(res.ok);
        if (res.ok) {
          const data = await res.json();
          setUsername(data.userName || null);
        } else {
          setUsername(null);
        }
      } catch {
        setLoggedIn(false);
        setUsername(null);
      }
      setCheckingLogin(false);
    }
    checkLogin();
  }, []);

  React.useEffect(() => {
    async function fetchWeatherWithAuth() {
      async function fetchWeather() {
        const getLocation = () => new Promise<GeolocationPosition>((resolve, reject) => {
          if (!navigator.geolocation) return reject("Geolocation not supported");
          navigator.geolocation.getCurrentPosition(resolve, reject);
        });
        let position;
        try {
          position = await getLocation();
        } catch {
          setWeatherError("Could not get location.");
          setShowWeather(false);
          return false;
        }
        const lat = position.coords.latitude;
        const lon = position.coords.longitude;
        const res = await fetch(`http://localhost:12001/api/v1/weather?latitude=${lat}&longitude=${lon}`, { credentials: "include" });
        if (res.status === 401) return "401";
        if (!res.ok) {
          setWeatherError("Failed to load weather.");
          setShowWeather(false);
          return false;
        }
        setWeather(await res.json());
        setShowWeather(true);
        setWeatherError(null);
        return true;
      }
      let result = await fetchWeather();
      if (result === "401") {
        // Try refresh token
        const refreshRes = await fetch(`${API_BASE}/auth/refresh`, { method: "POST", credentials: "include" });
        if (refreshRes.ok) {
          result = await fetchWeather();
          if (result === true) return;
        }
        setLoggedIn(false);
        setShowWeather(false);
        setWeather(null);
        setModal("login");
      }
    }
    if (loggedIn) fetchWeatherWithAuth();
  }, [loggedIn]);

  async function handleLogout() {
    await fetch(`${API_BASE}/auth/logout`, { method: "POST", credentials: "include" });
    setLoggedIn(false);
    setShowWeather(false);
    setWeather(null);
    setModal("login");
  }

  async function handleDeleteAccount() {
    await fetch(`${API_BASE}/auth/delete`, { method: "POST", credentials: "include" });
    setGlobalMessage("Account deleted.");
    handleLogout();
  }

  if (checkingLogin) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen w-full">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500 mb-4"></div>
        <div className="text-lg font-semibold text-blue-700">Loading...</div>
      </div>
    );
  }

  return (
    <div className="flex flex-row gap-8 w-full min-h-screen">
      {loggedIn && (
        <UserMenu
          username={username}
          onProfile={() => openModal("profile")}
          on2FA={() => openModal("2fa")}
          onChangePassword={() => openModal("changePassword")}
          onDelete={() => openModal("deleteAccount")}
          onLogout={handleLogout}
        />
      )}
      <main className="flex-1 flex flex-col items-center w-full">
        {globalMessage && modal !== "login" && (
          <div className="text-green-600 text-sm bg-green-100 dark:bg-green-900 rounded px-2 py-1 mb-4">{globalMessage}</div>
        )}
        {(() => {
          if (!loggedIn) return null;
          if (showWeather && weather) {
            return (
              <div className="w-full max-w-xl mx-auto gap-6 sm:gap-8 bg-white/80 dark:bg-gray-900/80 p-2 sm:p-4 md:p-6">
                {/* Current Weather Section */}
                <div className="flex flex-row justify-between items-center gap-2 sm:gap-4 my-3">
                  <div className="flex flex-col items-start gap-1 w-full sm:w-auto">
                    <div className="text-lg sm:text-3xl font-bold text-gray-900 dark:text-gray-100">Gouda</div>
                    <div className="text-4xl sm:text-5xl font-extrabold leading-none">{weather.current.temperature}°C</div>
                    <div className="text-base sm:text-lg font-medium text-gray-600 dark:text-gray-300">{getWeatherLabel(weather.current.weatherCode)}</div>
                    <div className="flex flex-row gap-4 sm:gap-6 mt-1 sm:mt-2 text-xs sm:text-sm text-gray-700 dark:text-gray-300">
                      <div>Wind: {weather.current.windSpeed} km/h {weather.current.windDirection}°</div>
                    </div>
                  </div>
                  <div className="flex flex-col items-end justify-center flex-1 w-full sm:w-auto mr-5">
                    {getWeatherIcon(weather.current.weatherCode, 96)}
                  </div>
                </div>
                {/* Hourly Forecast Section */}
                <div className="py-3 border-t border-gray-400 dark:border-gray-300">
                  <div className="text-base sm:text-lg font-semibold">Hourly Forecast</div>
                  <div className="flex flex-row gap-2 sm:gap-4 overflow-x-auto pb-2 scrollbar-hide" style={{ scrollbarWidth: 'none', msOverflowStyle: 'none' }}>
                    {weather.hourly.slice(0, 24).map((h, i) => (
                      <div key={i} className="flex flex-col items-center min-w-[48px] sm:min-w-[64px] p-1 sm:p-2 rounded-lg bg-gray-50 dark:bg-gray-800 border border-gray-200 dark:border-gray-700">
                        <div className="text-[10px] sm:text-xs font-medium text-gray-500 dark:text-gray-300 mb-0.5 sm:mb-1">
                          {i === 0 ? "Now" : new Date(h.time).toLocaleTimeString([], { hour: "2-digit", hour12: false })}
                        </div>
                        <div className="text-xl sm:text-2xl mb-0.5 sm:mb-1">{getWeatherIcon(h.weatherCode, 32)}</div>
                        <div className="font-bold text-base sm:text-lg">{Math.round(h.temperature)}°</div>
                        <div className="text-[10px] sm:text-xs text-blue-700 dark:text-blue-300">{h.precipitationProbability}%</div>
                      </div>
                    ))}
                  </div>
                </div>
                {/* 14-Day Forecast Section */}
                <div className="py-3 border-t border-gray-400 dark:border-gray-300">
                  <div className="text-base sm:text-lg font-semibold">14-Day Forecast</div>
                  <div className="overflow-x-auto">
                    <table className="w-full text-xs sm:text-sm">
                      <tbody>
                        {weather.daily.slice(0, 14).map((d, i) => (
                          <tr key={i} className="border-b border-gray-200 dark:border-gray-700 last:border-0">
                            <td className="py-1 pr-1 sm:pr-2 font-medium text-gray-700 dark:text-gray-200 text-left">
                              {i === 0 ? "Today" : new Date(d.time).toLocaleDateString("en-GB", { weekday: "short" })}
                            </td>
                            <td className="py-1 pr-1 sm:pr-2 text-center">{getWeatherIcon(d.weatherCode, 32)}</td>
                            <td className="py-1 pr-1 sm:pr-2 text-right font-bold">{d.temperatureMax}°C</td>
                            <td className="py-1 pr-1 sm:pr-2 text-right text-gray-500 dark:text-gray-400">{d.temperatureMin}°C</td>
                            <td className="py-1 pr-1 sm:pr-2 text-right" style={{ minWidth: 32, maxWidth: 48, width: 48 }}>
                              {d.precipitationProbabilityMax != null ? `${Math.round(d.precipitationProbabilityMax)}%` : "-"}
                            </td>
                            <td className="py-1 pr-1 sm:pr-2 text-right whitespace-nowrap" style={{ minWidth: 32, maxWidth: 48, width: 48 }}>
                              {d.precipitation != null ? `${d.precipitation.toFixed(1)} mm` : "-"}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            );
          }
          if (weatherError) {
            return (
              <div className="text-red-600 text-sm bg-red-100 dark:bg-red-900 rounded px-2 py-1 mb-4">{weatherError}</div>
            );
          }
          return null;
        })()}
      </main>
      <Modal open={!loggedIn && modal === "login"} onClose={closeModal}>
        <Login
          onSuccess={() => {
            setGlobalMessage("Login successful!");
            setLoggedIn(true);
            closeModal();
          }}
          onRegister={() => openModal("register")}
          onForgot={() => openModal("forgot")}
        />
      </Modal>
      <Modal open={modal === "register"} onClose={closeModal}>
        <Register
          onSuccess={() => {
            setGlobalMessage("Registration successful! You can now log in.");
            openModal("login");
          }}
          onLogin={() => openModal("login")}
        />
      </Modal>
      <Modal open={modal === "forgot"} onClose={closeModal}>
        <ForgotPassword
          onBack={() => openModal("login")}
          onReset={() => openModal("reset")}
        />
      </Modal>
      <Modal open={modal === "reset"} onClose={closeModal}>
        <ResetPassword
          onBack={() => openModal("forgot")}
          onLogin={() => openModal("login")}
        />
      </Modal>
      <Modal open={modal === "profile"} onClose={closeModal}>
        <Profile onClose={closeModal} />
      </Modal>
      <Modal open={modal === "2fa"} onClose={closeModal}>
        <Setup2FA onClose={closeModal} />
      </Modal>
      <Modal open={modal === "changePassword"} onClose={closeModal}>
        <ChangePassword onClose={closeModal} />
      </Modal>
      <Modal open={modal === "deleteAccount"} onClose={closeModal}>
        <div className="flex flex-col gap-4 items-center p-6">
          <h2 className="text-xl font-bold mb-2 text-red-700">Delete Account</h2>
          <p className="text-center">Are you sure you want to delete your account? This action cannot be undone.</p>
          <button className="bg-red-600 text-white rounded px-4 py-2 font-semibold" onClick={handleDeleteAccount}>Delete Account</button>
          <button className="bg-gray-600 text-white rounded px-4 py-2 font-semibold" onClick={closeModal}>Cancel</button>
        </div>
      </Modal>
    </div>
  );
}
