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
import DeleteAccountModal from "../components/DeleteAccountModal";
import ProfilePage from "../components/ProfilePage";
import SecurityPage from "../components/SecurityPage";
import { IconSun, IconUser, IconWind } from "@tabler/icons-react";
import type { Weather } from "../types/Weather";
import { weatherCodeMap } from "../types/WeatherCodeMap";

const isDev = typeof window !== "undefined" && window.location.hostname === "localhost";
const AUTH_API_BASE = isDev
  ? (process.env.NEXT_PUBLIC_API_URL || "http://localhost:12002")
  : "https://demo.markpost.dev";
const WEATHER_API_BASE = isDev
  ? (process.env.NEXT_PUBLIC_WEATHER_API_URL || "http://localhost:12001")
  : "https://demo.markpost.dev";

function getWeatherIcon(code: string, size = 32) {
  return weatherCodeMap[code]?.icon(size) || <IconSun size={size} />;
}
function getWeatherLabel(code: string) {
  return weatherCodeMap[code]?.label || code;
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
  const [twoFAEnabled, setTwoFAEnabled] = React.useState(false);
  const [checkingLogin, setCheckingLogin] = React.useState(true);
  const [activePage, setActivePage] = React.useState<null | "profile" | "security">(null);

  // Modal open/close helpers
  const openModal = (name: typeof modal) => setModal(name);
  const closeModal = () => setModal(null);

  React.useEffect(() => {
    async function checkLogin() {
      try {
        let res = await fetch(`${AUTH_API_BASE}/api/auth/v1/user`, { credentials: "include" });
        if (res.status === 401) {
          // Try refresh token
          const refreshRes = await fetch(`${AUTH_API_BASE}/api/auth/v1/refresh`, { method: "POST", credentials: "include" });
          if (refreshRes.ok) {
            res = await fetch(`${AUTH_API_BASE}/api/auth/v1/user`, { credentials: "include" });
          }
        }
        setLoggedIn(res.ok);
        if (res.ok) {
          const data = await res.json();
          setUsername(data.userName || null);
          setTwoFAEnabled(data.twoFAEnabled || false);
        } else {
          setUsername(null);
          setTwoFAEnabled(false);
        }
      } catch {
        setLoggedIn(false);
        setUsername(null);
        setTwoFAEnabled(false);
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
        const res = await fetch(`${WEATHER_API_BASE}/api/weather/v1/forecast?latitude=${lat}&longitude=${lon}`, { credentials: "include" });
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
        const refreshRes = await fetch(`${AUTH_API_BASE}/api/auth/v1/refresh`, { method: "POST", credentials: "include" });
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
    await fetch(`${AUTH_API_BASE}/api/auth/v1/logout`, { method: "POST", credentials: "include" });
    setLoggedIn(false);
    setShowWeather(false);
    setWeather(null);
    setModal("login");
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
        <div className="fixed top-1 right-1 z-50">
          <button
            className="flex items-center gap-2 bg-blue-600 text-white rounded-full px-3 py-1 shadow-lg hover:bg-blue-700 focus:outline-none"
            onClick={() => setActivePage("profile")}
            aria-label="Profile"
          >
            <IconUser size={18} />
            <span className="text-sm font-semibold">{username || "User"}</span>
          </button>
        </div>
      )}
      {/* Main content area: weather, profile, security */}
      <main className="flex-1 flex flex-col items-center w-full">
        {/* Render ProfilePage or SecurityPage as pages, not overlays */}
        {activePage === "profile" ? (
          <ProfilePage
            onBack={() => setActivePage(null)}
            onSecurity={() => setActivePage("security")}
            onDeleteAccount={() => setModal("deleteAccount")}
            onLogout={handleLogout}
          />
        ) : activePage === "security" ? (
          <SecurityPage
            onBack={() => setActivePage("profile")}
            onChangePassword={() => setModal("changePassword")}
            onToggle2FA={() => setModal("2fa")}
          />
        ) : (
          // Weather view
          (() => {
            if (!loggedIn) return null;
            if (showWeather && weather) {
              return (
                <div className="w-full max-w-xl mx-auto gap-6 sm:gap-8 bg-gradient-to-br from-blue-30 to-indigo-100 dark:from-gray-900/80 dark:to-gray-900/80 p-2 sm:p-4 md:p-6">
                  {/* Current Weather Section */}
                  <div className="flex flex-row justify-between items-center gap-2 sm:gap-4 my-3">
                    <div className="flex flex-col items-start gap-1 w-full sm:w-auto">
                      <div className="text-lg sm:text-3xl font-bold text-gray-900 dark:text-gray-100">{weather.location}</div>
                      <div className="text-4xl sm:text-5xl font-extrabold leading-none">{Math.round(weather.current.temperature)}°C</div>
                      <div className="text-base sm:text-lg font-medium text-gray-600 dark:text-gray-300">{getWeatherLabel(weather.current.weatherCode)}</div>
                      <div className="flex flex-row gap-4 sm:gap-6 mt-1 sm:mt-2 text-xs sm:text-sm text-gray-700 dark:text-gray-300">
                        <div><IconWind className="float-left mr-1" size="20" /> {weather.current.windSpeed} km/h - {weather.current.windDirection}</div>
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
                      {weather.hourly.slice(0, 48).map((h, i) => (
                        <div key={i} className="flex flex-col items-center min-w-[48px] sm:min-w-[64px] p-1 sm:p-2 rounded-lg bg-white/80 dark:bg-gray-800 border border-gray-200 dark:border-gray-700">
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
                              <td className="py-1 pr-1 sm:pr-2 w-16 font-medium text-gray-700 dark:text-gray-200 text-left">
                                {i === 0 ? "Today" : new Date(d.time).toLocaleDateString("en-GB", { weekday: "short" })}
                              </td>
                              <td className="py-1 pr-1 sm:pr-2 w-16 text-center">{getWeatherIcon(d.weatherCode, 32)}</td>
                              <td className="py-1 pr-1 sm:pr-2 w-10 text-center font-bold">{Math.round(d.temperatureMax)}°C</td>
                              <td className="py-1 pr-1 sm:pr-2 w-24 text-center text-gray-500 dark:text-gray-400">{Math.round(d.temperatureMin)}°C</td>
                              <td className="py-1 pr-1 sm:pr-2 text-center">
                                {d.precipitationProbabilityMax != null ? `${Math.round(d.precipitationProbabilityMax)}%` : "-"}
                              </td>
                              <td className="py-1 pr-1 sm:pr-2 text-right whitespace-nowrap">
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
          })()
        )}
      </main>
      {/* Modals for login, register, forgot, reset, profile (legacy), 2fa, changePassword, deleteAccount */}
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
      <Modal open={modal === "2fa"} onClose={closeModal}>
        <Setup2FA />
      </Modal>
      <Modal open={modal === "changePassword"} onClose={closeModal}>
        <ChangePassword onClose={closeModal} />
      </Modal>
      <Modal open={modal === "deleteAccount"} onClose={closeModal}>
        <DeleteAccountModal
          onSuccess={() => {
            setGlobalMessage("Account deleted.");
            handleLogout();
          }}
          onCancel={closeModal}
        />
      </Modal>
    </div>
  );
}
