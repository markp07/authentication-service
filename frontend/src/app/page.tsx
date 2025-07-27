"use client";

import React from "react";
import { useRouter } from "next/navigation";
import Modal from "./Modal";
import Login from "./Login";
import Register from "./Register";
import ForgotPassword from "./ForgotPassword";
import ResetPassword from "./ResetPassword";
import Profile from "./Profile";
import Setup2FA from "./Setup2FA";
import ChangePassword from "./ChangePassword";
import { IconTrash, IconShieldLock, IconSun, IconCloud, IconCloudFog, IconCloudRain, IconCloudSnow, IconLogout, IconUser, IconSunrise, IconSunset } from "@tabler/icons-react";

const API_BASE = "http://localhost:12002/v1";

const weatherCodeMap: Record<string, { label: string; icon: JSX.Element }> = {
  CLEAR_SKY: { label: "Clear sky", icon: <IconSun className="text-yellow-400" size={32} /> },
  MAINLY_CLEAR: { label: "Mainly clear", icon: <IconCloud className="text-blue-400" size={32} /> },
  PARTLY_CLOUDY: { label: "Partly cloudy", icon: <IconCloud className="text-blue-400" size={32} /> },
  OVERCAST: { label: "Overcast", icon: <IconCloud className="text-blue-400" size={32} /> },
  FOG: { label: "Fog", icon: <IconCloudFog className="text-gray-400" size={32} /> },
  DEPOSITING_RIME_FOG: { label: "Depositing rime fog", icon: <IconCloudFog className="text-gray-400" size={32} /> },
  DRIZZLE_LIGHT: { label: "Drizzle", icon: <IconCloudRain className="text-blue-400" size={32} /> },
  DRIZZLE_MODERATE: { label: "Drizzle", icon: <IconCloudRain className="text-blue-400" size={32} /> },
  DRIZZLE_DENSE: { label: "Drizzle", icon: <IconCloudRain className="text-blue-400" size={32} /> },
  FREEZING_DRIZZLE_LIGHT: { label: "Freezing Drizzle: Light", icon: <IconCloudRain className="text-blue-200" size={32} /> },
  FREEZING_DRIZZLE_DENSE: { label: "Freezing Drizzle: Dense", icon: <IconCloudRain className="text-blue-200" size={32} /> },
  RAIN_SLIGHT: { label: "Rain", icon: <IconCloudRain className="text-blue-600" size={32} /> },
  RAIN_MODERATE: { label: "Rain", icon: <IconCloudRain className="text-blue-600" size={32} /> },
  RAIN_HEAVY: { label: "Rain", icon: <IconCloudRain className="text-blue-600" size={32} /> },
  FREEZING_RAIN_LIGHT: { label: "Freezing Rain: Light", icon: <IconCloudRain className="text-blue-200" size={32} /> },
  FREEZING_RAIN_HEAVY: { label: "Freezing Rain: Heavy", icon: <IconCloudRain className="text-blue-200" size={32} /> },
  SNOW_SLIGHT: { label: "Snow fall: Slight", icon: <IconCloudSnow className="text-blue-200" size={32} /> },
  SNOW_MODERATE: { label: "Snow fall: Moderate", icon: <IconCloudSnow className="text-blue-200" size={32} /> },
  SNOW_HEAVY: { label: "Snow fall: Heavy", icon: <IconCloudSnow className="text-blue-200" size={32} /> },
  SNOW_GRAINS: { label: "Snow grains", icon: <IconCloudSnow className="text-blue-200" size={32} /> },
  RAIN_SHOWERS_SLIGHT: { label: "Rain showers: Slight", icon: <IconCloudRain className="text-blue-400" size={32} /> },
  RAIN_SHOWERS_MODERATE: { label: "Rain showers: Moderate", icon: <IconCloudRain className="text-blue-400" size={32} /> },
  RAIN_SHOWERS_VIOLENT: { label: "Rain showers: Violent", icon: <IconCloudRain className="text-blue-400" size={32} /> },
  SNOW_SHOWERS_SLIGHT: { label: "Snow showers: Slight", icon: <IconCloudSnow className="text-blue-400" size={32} /> },
  SNOW_SHOWERS_HEAVY: { label: "Snow showers: Heavy", icon: <IconCloudSnow className="text-blue-400" size={32} /> },
  THUNDERSTORM_SLIGHT_MODERATE: { label: "Thunderstorm: Slight or moderate", icon: <IconCloud className="text-yellow-600" size={32} /> },
  THUNDERSTORM_SLIGHT_HAIL: { label: "Thunderstorm with slight hail", icon: <IconCloudSnow className="text-blue-400" size={32} /> },
  THUNDERSTORM_HEAVY_HAIL: { label: "Thunderstorm with heavy hail", icon: <IconCloudSnow className="text-blue-400" size={32} /> },
};
function getWeatherIcon(code: string) {
  return weatherCodeMap[code]?.icon || <IconSun size={32} />;
}
function getWeatherLabel(code: string) {
  return weatherCodeMap[code]?.label || code;
}

function Sidebar({ onProfile, on2FA, onChangePassword, onDelete, onLogout, mobileOpen, setMobileOpen }) {
  return (
    <>
      {/* Mobile menu button */}
      <button
        className="md:hidden fixed top-4 left-4 z-50 bg-blue-600 text-white rounded-full p-2 shadow-lg"
        onClick={() => setMobileOpen(true)}
        aria-label="Open menu"
        style={{ display: mobileOpen ? 'none' : 'block' }}
      >
        <svg width="28" height="28" fill="none" viewBox="0 0 24 24"><path stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" d="M4 6h16M4 12h16M4 18h16"/></svg>
      </button>
      {/* Sidebar menu */}
      <aside
        className={`md:static md:block fixed top-0 left-0 h-full w-64 bg-white/90 dark:bg-gray-900/90 rounded-lg shadow-lg p-6 border border-blue-200 dark:border-gray-800 z-50 transition-transform duration-300 ${mobileOpen ? 'translate-x-0' : '-translate-x-full'} md:translate-x-0`}
        style={{ minWidth: '220px' }}
      >
        <button
          className="md:hidden absolute top-4 right-4 text-gray-500 hover:text-gray-900 dark:hover:text-white"
          onClick={() => setMobileOpen(false)}
          aria-label="Close menu"
        >
          ×
        </button>
        <div className="flex flex-col gap-4 mt-8 md:mt-0">
          <button className="flex items-center gap-2 px-4 py-2 rounded bg-blue-600 text-white hover:bg-blue-700 font-semibold" onClick={onProfile}>
            <IconUser size={20} /> Profile
          </button>
          <button className="flex items-center gap-2 px-4 py-2 rounded bg-purple-600 text-white hover:bg-purple-700 font-semibold" onClick={onChangePassword}>
            <IconShieldLock size={20} /> Change Password
          </button>
          <button className="flex items-center gap-2 px-4 py-2 rounded bg-green-600 text-white hover:bg-green-700 font-semibold" onClick={on2FA}>
            <IconShieldLock size={20} /> Enable 2FA
          </button>
          <button className="flex items-center gap-2 px-4 py-2 rounded bg-red-600 text-white hover:bg-red-700 font-semibold" onClick={onDelete}>
            <IconTrash size={20} /> Delete Account
          </button>
          <button className="flex items-center gap-2 px-4 py-2 rounded bg-gray-600 text-white hover:bg-gray-700 font-semibold" onClick={onLogout}>
            <IconLogout size={20} /> Logout
          </button>
        </div>
      </aside>
    </>
  );
}

export default function Home() {
  const router = useRouter();
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
  const [weather, setWeather] = React.useState<any | null>(null);
  const [loggedIn, setLoggedIn] = React.useState(false);
  const [mobileOpen, setMobileOpen] = React.useState(false);

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
      } catch {
        setLoggedIn(false);
      }
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
        } catch (e) {
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

  return (
    <div className="flex flex-row gap-8 w-full min-h-screen">
      {loggedIn && (
        <Sidebar
          onProfile={() => openModal("profile")}
          on2FA={() => openModal("2fa")}
          onChangePassword={() => openModal("changePassword")}
          onDelete={() => openModal("deleteAccount")}
          onLogout={handleLogout}
          mobileOpen={mobileOpen}
          setMobileOpen={setMobileOpen}
        />
      )}
      <main className="flex-1 flex flex-col items-center justify-center p-4 md:p-8">
        <h1 className="text-3xl md:text-4xl font-bold text-blue-700 dark:text-blue-200 mb-6 flex items-center gap-2">
          <IconCloud size={32} className="md:size-40" /> Weather App
        </h1>
        {globalMessage && modal !== "login" && (
          <div className="text-green-600 text-sm bg-green-100 dark:bg-green-900 rounded px-2 py-1 mb-4">{globalMessage}</div>
        )}
        {!loggedIn ? null : showWeather && weather ? (
          <div className="w-full flex flex-col items-center justify-center">
            <div className="mb-6 text-center flex flex-col items-center">
              <div className="text-lg font-semibold mb-2">Now</div>
              <div className="flex items-center justify-center gap-2 text-4xl font-bold text-blue-700 dark:text-blue-200">
                {getWeatherIcon(weather.current.weatherCode)}
                {weather.current.temperature}°C
              </div>
            </div>
            <div>
              <div className="text-lg font-semibold mb-2">Daily Forecast</div>
              <div className="overflow-x-auto">
                <table className="min-w-full text-sm">
                  <tbody>
                    {weather.daily.map((d: any, i: number) => (
                      <tr key={i} className={i % 2 === 0 ? "bg-blue-50 dark:bg-gray-900" : "bg-white dark:bg-gray-800"}>
                        <td className="px-2 py-1 whitespace-nowrap text-center">
                          {new Date(d.time).toLocaleDateString("en-GB", { weekday: "short" })}<br />
                          {new Date(d.time).toLocaleDateString("en-GB", { day: "2-digit", month: "2-digit" })}
                        </td>
                        <td className="px-2 py-1 items-center text-center">
                          {getWeatherIcon(d.weatherCode)}
                        </td>
                        <td className="px-2 py-1 text-center">
                          <span className="font-bold">{d.temperatureMin}°C</span> / <span className="font-bold">{d.temperatureMax}°C</span>
                        </td>
                        <td className="px-2 py-1 text-center">
                          {d.precipitation.toFixed(1)}mm
                        </td>
                        <td className="px-2 py-1 text-center">
                          <span className="inline-flex items-center gap-1">
                            <IconSunrise className="text-yellow-500" size={20} />
                            {new Date(d.sunRise).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}
                          </span>
                          <br/>
                          <span className="inline-flex items-center gap-1">
                            <IconSunset className="text-orange-500" size={20} />
                            {new Date(d.sunSet).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        ) : weatherError ? (
          <div className="text-red-600 text-sm bg-red-100 dark:bg-red-900 rounded px-2 py-1 mb-4">{weatherError}</div>
        ) : null}
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
        <Profile
          onClose={closeModal}
          onSetup2FA={() => openModal("2fa")}
          onChangePassword={() => openModal("changePassword")}
        />
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
