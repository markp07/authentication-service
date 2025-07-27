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
import { IconSun, IconCloud, IconCloudFog, IconCloudRain, IconCloudSnow, IconLogout, IconUser, IconSunrise, IconSunset } from "@tabler/icons-react";

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

interface WeatherData {
  latitude: number;
  longitude: number;
  timezone: string;
  elevation: number;
  current: {
    time: string;
    weatherCode: string;
    temperature: number;
    windSpeed: number;
  };
  daily: Array<{
    time: string;
    sunRise: string;
    sunSet: string;
    weatherCode: string;
    temperatureMin: number;
    temperatureMax: number;
    precipitation: number;
  }>;
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
    | null
  >("login");
  const [globalMessage, setGlobalMessage] = React.useState<string | null>(null);
  const [showWeather, setShowWeather] = React.useState(false);
  const [weatherError, setWeatherError] = React.useState<string | null>(null);
  const [weather, setWeather] = React.useState<WeatherData | null>(null);

  // Modal open/close helpers
  const openModal = (name: typeof modal) => setModal(name);
  const closeModal = () => setModal(null);

  React.useEffect(() => {
    async function tryLoadWeather() {
      try {
        // Try to get geolocation
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
          setModal("login");
          return;
        }
        const lat = position.coords.latitude;
        const lon = position.coords.longitude;
        // Try to fetch weather
        const res = await fetch(`http://localhost:12001/api/v1/weather?latitude=${lat}&longitude=${lon}`, { credentials: "include" });
        if (res.status === 401) {
          // Try to refresh token
          const refreshRes = await fetch("http://localhost:12002/v1/auth/refresh", { method: "POST", credentials: "include" });
          if (refreshRes.status === 401) {
            setWeatherError("Session expired. Please login.");
            setShowWeather(false);
            setModal("login");
            return;
          }
          // Try weather fetch again
          const retryRes = await fetch(`http://localhost:12001/api/v1/weather?latitude=${lat}&longitude=${lon}`, { credentials: "include" });
          if (!retryRes.ok) {
            setWeatherError("Failed to load weather after refresh.");
            setShowWeather(false);
            setModal("login");
            return;
          }
          setWeather(await retryRes.json());
          setShowWeather(true);
          setWeatherError(null);
          setModal(null);
          return;
        }
        if (!res.ok) {
          setWeatherError("Failed to load weather.");
          setShowWeather(false);
          setModal("login");
          return;
        }
        setWeather(await res.json());
        setShowWeather(true);
        setWeatherError(null);
        setModal(null);
      } catch (e: any) {
        setWeatherError(e.message || "Unknown error");
        setShowWeather(false);
        setModal("login");
      }
    }
    tryLoadWeather();
  }, []);

  return (
    <div className="font-sans min-h-screen flex flex-col items-center justify-center p-8 bg-gradient-to-br from-blue-100 via-white to-blue-300 dark:from-gray-900 dark:via-gray-800 dark:to-gray-700">
      <main className="w-full max-w-2xl bg-white/80 dark:bg-gray-900/80 rounded-lg shadow-2xl p-8 border border-blue-200 dark:border-gray-800 backdrop-blur">
        <div className="flex justify-between items-center mb-4">
          <h1 className="text-3xl font-bold text-blue-700 dark:text-blue-200">Weather</h1>
          <button
            className="flex items-center gap-2 px-4 py-2 rounded bg-blue-600 text-white hover:bg-blue-700 transition"
            onClick={() => openModal("profile")}
          >
            <IconUser size={20} /> Profile
          </button>
        </div>
        {weatherError && (
          <div className="text-red-600 text-sm bg-red-100 dark:bg-red-900 rounded px-2 py-1 mb-4">{weatherError}</div>
        )}
        {showWeather && weather ? (
          <>
            <div className="flex justify-end gap-4 mb-4">
              <button
                className="flex items-center gap-2 px-4 py-2 rounded bg-red-600 text-white hover:bg-red-700 transition"
                onClick={async () => {
                  await fetch("http://localhost:12002/v1/auth/logout", { method: "POST", credentials: "include" });
                  window.location.href = "/";
                }}
              >
                <IconLogout size={20} /> Logout
              </button>
            </div>
            <div className="mb-6 text-center flex flex-col items-center">
              <div className="text-lg font-semibold">Current Weather</div>
              <div className="mb-2 text-blue-700 dark:text-blue-200 font-bold text-xl flex items-center justify-center gap-2">
                {getWeatherIcon(weather.current.weatherCode)}
                {getWeatherLabel(weather.current.weatherCode)}
              </div>
              <div>Location: {weather.latitude.toFixed(4)}, {weather.longitude.toFixed(4)}</div>
              <div>Temperature: <span className="font-bold">{weather.current.temperature}°C</span></div>
              <div>Wind Speed: {weather.current.windSpeed} km/h</div>
              <div>Time: {new Date(weather.current.time).toLocaleString()}</div>
            </div>
            <div>
              <div className="text-lg font-semibold mb-2">Daily Forecast</div>
              <div className="overflow-x-auto">
                <table className="min-w-full text-sm">
                  <tbody>
                    {weather.daily.map((d, i) => (
                      <tr key={i} className={i % 2 === 0 ? "bg-blue-50 dark:bg-gray-900" : "bg-white dark:bg-gray-800"}>
                        <td className="px-2 py-1 whitespace-nowrap text-center">
                          {new Date(d.time).toLocaleDateString("en-GB", { weekday: "short" })}
                          <br />
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
                          <IconSunrise className="text-black" size={20} /> {new Date(d.sunRise).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })} <br/>
                          <IconSunset className="text-black" size={20} /> {new Date(d.sunSet).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </>
        ) : (
          <div className="flex flex-col gap-4">
            <button
              className="bg-blue-500 text-white px-4 py-2 rounded shadow hover:bg-blue-600"
              onClick={() => openModal("login")}
            >
              Login
            </button>
            <button
              className="bg-green-500 text-white px-4 py-2 rounded shadow hover:bg-green-600"
              onClick={() => openModal("register")}
            >
              Register
            </button>
            <button
              className="bg-gray-500 text-white px-4 py-2 rounded shadow hover:bg-gray-600"
              onClick={() => openModal("forgot")}
            >
              Forgot Password
            </button>
          </div>
        )}
      </main>
      <Modal open={modal === "login"} onClose={closeModal}>
        <Login
          onSuccess={() => {
            setGlobalMessage("Login successful!");
            closeModal();
            window.location.reload();
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
    </div>
  );
}
