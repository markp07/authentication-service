"use client";
import React, { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

interface WeatherData {
  latitude: number;
  longitude: number;
  timezone: string;
  elevation: number;
  current: {
    time: string;
    temperature: number;
    windSpeed: number;
    relativeHumidity: number;
  };
  hourly: Array<{
    time: string;
    temperature: number;
    windSpeed: number;
    relativeHumidity: number;
  }>;
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
            <div className="mb-6 text-center">
              <div className="text-lg font-semibold">Current Weather</div>
              <div>Location: {weather.latitude.toFixed(4)}, {weather.longitude.toFixed(4)}</div>
              <div>Temperature: <span className="font-bold">{weather.current.temperature}°C</span></div>
              <div>Wind Speed: {weather.current.windSpeed} km/h</div>
              <div>Humidity: {weather.current.relativeHumidity}%</div>
              <div>Time: {new Date(weather.current.time).toLocaleString()}</div>
            </div>
            <div>
              <div className="text-lg font-semibold mb-2">Hourly Forecast</div>
              <div className="overflow-x-auto">
                <table className="min-w-full text-sm">
                  <thead>
                    <tr className="bg-blue-200 dark:bg-gray-800">
                      <th className="px-2 py-1">Time</th>
                      <th className="px-2 py-1">Temp (°C)</th>
                      <th className="px-2 py-1">Wind (km/h)</th>
                      <th className="px-2 py-1">Humidity (%)</th>
                    </tr>
                  </thead>
                  <tbody>
                    {weather.hourly.map((h, i) => (
                      <tr key={i} className={i % 2 === 0 ? "bg-blue-50 dark:bg-gray-900" : "bg-white dark:bg-gray-800"}>
                        <td className="px-2 py-1">{new Date(h.time).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}</td>
                        <td className="px-2 py-1">{h.temperature}</td>
                        <td className="px-2 py-1">{h.windSpeed}</td>
                        <td className="px-2 py-1">{h.relativeHumidity}</td>
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
