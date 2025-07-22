"use client";
import React, { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { IconSun, IconCloud, IconCloudFog, IconCloudRain, IconCloudSnow, IconSunrise, IconSunset } from "@tabler/icons-react";

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

export default function WeatherPage() {
  const router = useRouter();
  const [weather, setWeather] = useState<WeatherData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  async function fetchWeatherWithAuth(lat: number, lon: number) {
    setLoading(true);
    setError(null);
    try {
      const res = await fetch(
        `http://localhost:12001/api/v1/weather?latitude=${lat}&longitude=${lon}`,
        {
          credentials: "include"
        }
      );
      if (res.status === 401) {
        // Try to refresh token
        const refreshRes = await fetch("http://localhost:12002/v1/auth/refresh", {
          method: "POST",
          credentials: "include"
        });
        if (refreshRes.status === 401) {
          router.push("/");
          return;
        }
        // Try weather fetch again
        const retryRes = await fetch(
          `http://localhost:12001/api/v1/weather?latitude=${lat}&longitude=${lon}`,
          {
            credentials: "include"
          }
        );
        if (!retryRes.ok) throw new Error("Failed to fetch weather data after refresh");
        setWeather(await retryRes.json());
        setLoading(false);
        return;
      }
      if (!res.ok) throw new Error("Failed to fetch weather data");
      setWeather(await res.json());
      setLoading(false);
    } catch (err: any) {
      setError(err.message);
      setLoading(false);
    }
  }

  useEffect(() => {
    if (!navigator.geolocation) {
      setError("Geolocation is not supported by your browser.");
      setLoading(false);
      return;
    }
    navigator.geolocation.getCurrentPosition(
      position => {
        const lat = position.coords.latitude;
        const lon = position.coords.longitude;
        fetchWeatherWithAuth(lat, lon);
      },
      () => {
        setError("Unable to retrieve your location.");
        setLoading(false);
      }
    );
  }, []);

  return (
    <div className="min-h-screen flex flex-col items-center justify-center p-8 bg-gradient-to-br from-blue-100 via-white to-blue-300 dark:from-gray-900 dark:via-gray-800 dark:to-gray-700">
      <div className="w-full max-w-2xl bg-white/80 dark:bg-gray-900/80 rounded-lg shadow-2xl p-8 border border-blue-200 dark:border-gray-800 backdrop-blur">
        <h1 className="text-3xl font-bold mb-6 text-blue-700 dark:text-blue-200 text-center">Weather</h1>
        {loading && <p className="text-center">Loading weather data...</p>}
        {error && <p className="text-red-600 text-center">{error}</p>}
        {weather && (
          <div>
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
          </div>
        )}
      </div>
    </div>
  );
}
