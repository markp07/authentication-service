"use client";

import React from "react";
import { useRouter } from "next/navigation";
import Modal from "../components/Modal";
import Sidebar from "../components/Sidebar";
import HourlyGraphModal from "../components/HourlyGraphModal";
import LocationSearch from "../components/LocationSearch";
import LocationBar from "../components/LocationBar";
import { IconArrowUp, IconArrowUpLeft, IconArrowUpRight, IconArrowDown, IconArrowDownLeft, IconArrowDownRight, IconArrowRight, IconArrowLeft } from "@tabler/icons-react";
import { Sun, Crosshair, GraphUp, Wind } from 'react-bootstrap-icons';
import type { Weather } from "../types/Weather";
import type { Location } from "../types/Location";
import { weatherCodeMap, isNightTime } from "../types/WeatherCodeMap";

const isDev = typeof window !== "undefined" && window.location.hostname === "localhost";
const AUTH_API_BASE = isDev
  ? (process.env.NEXT_PUBLIC_API_URL || "http://localhost:12002")
  : "https://demo.markpost.dev";
const WEATHER_API_BASE = isDev
  ? (process.env.NEXT_PUBLIC_WEATHER_API_URL || "http://localhost:12001")
  : "https://demo.markpost.dev";

function getWeatherIcon(code: string, size = 32, currentTime?: string, sunRise?: string, sunSet?: string) {
  const isNight = currentTime && sunRise && sunSet ? isNightTime(currentTime, sunRise, sunSet) : false;
  return weatherCodeMap[code]?.icon(size, isNight) || <Sun size={size} />;
}
function getWeatherLabel(code: string) {
  return weatherCodeMap[code]?.label || code;
}
function getWindDirectionIcon(direction: string, size = 22) {
  const iconMap: { [key: string]: any } = {
    S: IconArrowUp,
    SE: IconArrowUpLeft,
    SW: IconArrowUpRight,
    N: IconArrowDown,
    NE: IconArrowDownLeft,
    NW: IconArrowDownRight,
    W: IconArrowRight,
    E: IconArrowLeft
  };

  const IconComponent = iconMap[direction] || IconArrowUp;
  return <IconComponent size={size} />;
}



export default function Home() {
  const router = useRouter();
  const [showWeather, setShowWeather] = React.useState(false);
  const [weatherError, setWeatherError] = React.useState<string | null>(null);
  const [weather, setWeather] = React.useState<Weather | null>(null);
  const [loggedIn, setLoggedIn] = React.useState(false);
  const [username, setUsername] = React.useState<string | null>(null);
  const [checkingLogin, setCheckingLogin] = React.useState(true);
  const [showHourlyGraph, setShowHourlyGraph] = React.useState(false);
  const [selectedWeather, setSelectedWeather] = React.useState<Weather | null>(null);
  
  // Saved locations state
  const [savedLocations, setSavedLocations] = React.useState<Location[]>([]);
  const [savedWeatherData, setSavedWeatherData] = React.useState<Map<number, Weather>>(new Map());
  const [loadingWeather, setLoadingWeather] = React.useState<Set<number>>(new Set());
  
  // UI state
  const [showLocationSearchModal, setShowLocationSearchModal] = React.useState(false);
  const [selectedLocationId, setSelectedLocationId] = React.useState<number | null>(null); // null = current location
  const [displayWeather, setDisplayWeather] = React.useState<Weather | null>(null);


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
        } else {
          setUsername(null);
          // Redirect to login if not authenticated
          router.push("/login?callback=" + encodeURIComponent("/"));
        }
      } catch {
        setLoggedIn(false);
        setUsername(null);
        router.push("/login?callback=" + encodeURIComponent("/"));
      }
      setCheckingLogin(false);
    }
    checkLogin();
  }, [router]);

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
        router.push("/login?callback=" + encodeURIComponent("/"));
      }
    }
    if (loggedIn) fetchWeatherWithAuth();
  }, [loggedIn]);

  // Load saved locations from backend on mount
  React.useEffect(() => {
    async function loadSavedLocations() {
      if (loggedIn) {
        try {
          const { getSavedLocations } = await import("../utils/api");
          const locations = await getSavedLocations();
          setSavedLocations(locations);
        } catch (e) {
          console.error("Failed to load saved locations:", e);
        }
      }
    }
    loadSavedLocations();
  }, [loggedIn]);

  // Fetch weather for saved locations
  React.useEffect(() => {
    if (!loggedIn || savedLocations.length === 0) return;

    async function fetchWeatherForLocation(location: Location) {
      setLoadingWeather(prev => new Set(prev).add(location.id));
      try {
        const res = await fetch(
          `${WEATHER_API_BASE}/api/weather/v1/forecast?latitude=${location.latitude}&longitude=${location.longitude}`,
          { credentials: "include" }
        );
        if (res.ok) {
          const data: Weather = await res.json();
          setSavedWeatherData(prev => new Map(prev).set(location.id, data));
        }
      } catch (error) {
        console.error(`Failed to fetch weather for ${location.name}:`, error);
      } finally {
        setLoadingWeather(prev => {
          const next = new Set(prev);
          next.delete(location.id);
          return next;
        });
      }
    }

    savedLocations.forEach(location => {
      fetchWeatherForLocation(location);
    });
  }, [loggedIn, savedLocations]);

  const handleLocationSelect = async (location: Location) => {
    if (savedLocations.some(loc => loc.id === location.id)) {
      return; // Already saved
    }

    try {
      const { saveLocation } = await import("../utils/api");
      await saveLocation(location);
      setSavedLocations(prev => [...prev, location]);
    } catch (e) {
      console.error("Failed to save location:", e);
      alert("Failed to save location. Please try again.");
    }
  };

  const handleRemoveLocation = async (locationId: number) => {
    // Find the saved location to get its database ID
    const savedLocation = savedLocations.find(loc => loc.id === locationId);
    if (!savedLocation) return;

    try {
      const { deleteSavedLocation } = await import("../utils/api");
      await deleteSavedLocation(locationId);
      setSavedLocations(prev => prev.filter(loc => loc.id !== locationId));
      setSavedWeatherData(prev => {
        const next = new Map(prev);
        next.delete(locationId);
        return next;
      });
    } catch (e) {
      console.error("Failed to delete location:", e);
      alert("Failed to delete location. Please try again.");
    }
  };

  const handleReorderLocations = async (locationIds: number[]) => {
    try {
      const { reorderSavedLocations } = await import("../utils/api");
      await reorderSavedLocations(locationIds);
      // Optimistically update the order
      const reorderedLocations = locationIds
        .map(id => savedLocations.find(loc => loc.id === id))
        .filter((loc): loc is Location => loc !== undefined);
      setSavedLocations(reorderedLocations);
    } catch (e) {
      console.error("Failed to reorder locations:", e);
      alert("Failed to reorder locations. Please try again.");
      // Reload locations from server on error
      try {
        const { getSavedLocations } = await import("../utils/api");
        const locations = await getSavedLocations();
        setSavedLocations(locations);
      } catch (reloadError) {
        console.error("Failed to reload locations:", reloadError);
      }
    }
  };

  const handleLocationClick = (locationId: number | null) => {
    setSelectedLocationId(locationId);
    if (locationId === null) {
      // Show current location weather
      setDisplayWeather(weather);
    } else {
      // Show saved location weather
      const savedWeather = savedWeatherData.get(locationId);
      if (savedWeather) {
        setDisplayWeather(savedWeather);
      }
    }
  };
  
  // Update display weather when current weather or saved weather data changes
  React.useEffect(() => {
    if (selectedLocationId === null && weather) {
      setDisplayWeather(weather);
    } else if (selectedLocationId !== null) {
      const savedWeather = savedWeatherData.get(selectedLocationId);
      if (savedWeather) {
        setDisplayWeather(savedWeather);
      }
    }
  }, [weather, savedWeatherData, selectedLocationId]);

  async function handleLogout() {
    await fetch(`${AUTH_API_BASE}/api/auth/v1/logout`, { method: "POST", credentials: "include" });
    setLoggedIn(false);
    setShowWeather(false);
    setWeather(null);
    router.push("/login");
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

  return (
    <div className="flex min-h-screen w-full bg-gray-50 dark:bg-gray-900">
      {loggedIn && (
        <Sidebar
          username={username}
          activePage="dashboard"
          onNavigate={handleNavigate}
          onLogout={handleLogout}
        />
      )}
      
      {/* Main content area */}
      <main className="flex-1 overflow-auto lg:ml-64">
        {loggedIn ? (
          <div className="p-2 sm:p-4 lg:p-6">
                {showWeather && weather ? (
                  <div className="max-w-6xl mx-auto space-y-2 sm:space-y-4">

                    {/* Main Weather Display */}
                    {displayWeather && (
                    <>
                    <div className="bg-gradient-to-br from-blue-500 to-blue-700 dark:from-blue-700 dark:to-blue-900 rounded-xl shadow-xl p-3 sm:p-5 lg:p-6 text-white">
                      <div className="flex items-stretch justify-between gap-3">
                        <div className="flex-1">
                          <div className="flex items-center gap-2 mb-1">
                            <h2 className="text-2xl sm:text-3xl font-bold">{displayWeather.location}</h2>
                            {selectedLocationId === null && <Crosshair size={24} className="text-white" />}
                          </div>
                          <div className="text-5xl sm:text-6xl font-extrabold my-3 sm:my-4">{Math.round(displayWeather.current.temperature)}°C</div>
                          <div className="text-lg sm:text-xl font-medium opacity-90 mb-2 sm:mb-3">{getWeatherLabel(displayWeather.current.weatherCode)}</div>
                          <div className="flex items-center gap-3 sm:gap-4 text-sm opacity-90">
                            <div className="flex items-center gap-1.5 sm:gap-2">
                              <Wind size={18} />
                              <span>{displayWeather.current.windSpeed} km/h</span>
                              {getWindDirectionIcon(displayWeather.current.windDirection, 18)}
                            </div>
                          </div>
                        </div>
                        <div className="flex items-center justify-center self-stretch">
                          {getWeatherIcon(displayWeather.current.weatherCode, 160, displayWeather.current.time, displayWeather.daily[0]?.sunRise, displayWeather.daily[0]?.sunSet)}
                        </div>
                      </div>
                    </div>
                    {/* Horizontal Scrollable Location Bar */}
                    <LocationBar
                      currentLocationWeather={weather}
                      savedLocations={savedLocations}
                      savedWeatherData={savedWeatherData}
                      loadingWeather={loadingWeather}
                      selectedLocationId={selectedLocationId}
                      onLocationClick={handleLocationClick}
                      onRemoveLocation={(locationId) => {
                        handleRemoveLocation(locationId);
                        if (selectedLocationId === locationId) {
                          handleLocationClick(null);
                        }
                      }}
                      onReorderLocations={handleReorderLocations}
                      onAddLocationClick={() => setShowLocationSearchModal(true)}
                    />

                    {/* Hourly Forecast Card */}
                    <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-2 sm:p-5 lg:p-6">
                      <div className="flex justify-between items-center mb-2 sm:mb-4">
                        <h3 className="text-lg sm:text-xl font-bold text-gray-900 dark:text-white">Hourly Forecast</h3>
                        <button
                          onClick={() => setShowHourlyGraph(true)}
                          className="px-4 py-1 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium transition-colors text-sm"
                        >
                          <GraphUp size={20} />
                        </button>
                      </div>
                      <div className="flex gap-2 sm:gap-3 overflow-x-auto scrollbar-hide" style={{ scrollbarWidth: 'none', msOverflowStyle: 'none' }}>
                        {displayWeather.hourly.slice(0, 48).map((h, i) => {
                          // Find the corresponding daily data for this hour
                          const hourDate = new Date(h.time).toDateString();
                          const dailyData = displayWeather.daily.find(d => new Date(d.time).toDateString() === hourDate);
                          return (
                          <div key={i} className="flex flex-col items-center min-w-[65px] sm:min-w-[80px] p-2 sm:p-3 rounded-lg bg-gray-50 dark:bg-gray-700 border border-gray-200 dark:border-gray-600">
                            <div className="text-[10px] sm:text-xs font-medium text-gray-600 dark:text-gray-400 mb-1 sm:mb-2">
                              {i === 0 ? "Now" : new Date(h.time).toLocaleTimeString([], { hour: "2-digit", hour12: false })}
                            </div>
                            <div className="mb-1 sm:mb-2">{getWeatherIcon(h.weatherCode, 32, h.time, dailyData?.sunRise, dailyData?.sunSet)}</div>
                            <div className="font-bold text-base sm:text-lg text-gray-900 dark:text-white">{Math.round(h.temperature)}°</div>
                            <div className="text-[10px] sm:text-xs text-blue-600 dark:text-blue-400 mt-0.5 sm:mt-1">{h.precipitationProbability}%</div>
                            <div className="text-[10px] sm:text-xs text-gray-500 dark:text-gray-400">{h.precipitation.toFixed(1)}mm</div>
                            <div className="flex items-center gap-0.5 sm:gap-1 text-[10px] sm:text-xs text-gray-600 dark:text-gray-400 mt-0.5 sm:mt-1">
                              <span>{h.windSpeed}km/h</span>
                              {getWindDirectionIcon(h.windDirection, 12)}
                            </div>
                          </div>
                        )})}
                      </div>
                    </div>

                    {/* 14-Day Forecast Card */}
                    <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-2 sm:p-5 lg:p-6">
                      <h3 className="text-lg sm:text-xl font-bold text-gray-900 dark:text-white mb-3 sm:mb-4">14-Day Forecast</h3>
                      <div className="space-y-1 sm:space-y-2">
                        {displayWeather.daily.slice(0, 14).map((d, i) => {
                          // For daily forecast, use noon (12:00) to determine day/night
                          const noonTime = new Date(d.time);
                          noonTime.setHours(12, 0, 0, 0);
                          return (
                          <div key={i} className="flex items-center gap-2 sm:gap-3 p-2 sm:p-3 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors">
                            <div className="flex-1 min-w-0 text-xs sm:text-sm font-medium text-gray-700 dark:text-gray-300">
                              {i === 0 ? "Today" : new Date(d.time).toLocaleDateString("en-GB", { weekday: "short" })}
                            </div>
                            <div className="flex items-center justify-center w-8 sm:w-10 flex-shrink-0">
                              {getWeatherIcon(d.weatherCode, 28, noonTime.toISOString(), d.sunRise, d.sunSet)}
                            </div>
                            <div className="flex-1 font-bold text-sm sm:text-base text-gray-900 dark:text-white text-right">
                              {Math.round(d.temperatureMax)}°
                            </div>
                            <div className="flex-1 text-xs sm:text-sm text-gray-500 dark:text-gray-400 text-right">
                              {Math.round(d.temperatureMin)}°
                            </div>
                            <div className="flex-1 text-xs sm:text-sm text-blue-600 dark:text-blue-400 text-center">
                              {d.precipitationProbabilityMax != null ? `${Math.round(d.precipitationProbabilityMax)}%` : "-"}
                            </div>
                            <div className="flex-1 text-xs sm:text-sm text-gray-600 dark:text-gray-400 text-center">
                              {d.precipitation != null ? `${d.precipitation.toFixed(1)}mm` : "-"}
                            </div>
                            <div className="flex-1 flex items-center justify-end gap-0.5 sm:gap-1 text-xs sm:text-sm text-gray-600 dark:text-gray-400">
                              <span>{d.windSpeed}km/h</span>
                              {getWindDirectionIcon(d.windDirection, 14)}
                            </div>
                          </div>
                        )})}
                      </div>
                    </div>
                    </>
                    )}
                  </div>
                ) : weatherError ? (
                  <div className="max-w-4xl mx-auto">
                    <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg p-4">
                      <p className="text-red-600 dark:text-red-400">{weatherError}</p>
                    </div>
                  </div>
                ) : (
                  <div className="flex items-center justify-center h-full">
                    <div className="text-center">
                      <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500 mx-auto mb-4"></div>
                      <p className="text-gray-600 dark:text-gray-400">Loading weather data...</p>
                    </div>
                  </div>
                )}
              </div>
        ) : null}
      </main>

      {/* Hourly Graph Modal */}
      {displayWeather && (
        <HourlyGraphModal
          open={showHourlyGraph}
          onClose={() => setShowHourlyGraph(false)}
          hourlyData={displayWeather.hourly}
          dailyData={displayWeather.daily}
        />
      )}

      {/* Location Search Modal */}
      <Modal open={showLocationSearchModal} onClose={() => setShowLocationSearchModal(false)}>
        <div className="p-4">
          <h2 className="text-2xl font-bold mb-4 text-gray-900 dark:text-white">Search Location</h2>
          <LocationSearch
            weatherApiBase={WEATHER_API_BASE}
            onLocationSelect={(loc) => {
              handleLocationSelect(loc);
              setShowLocationSearchModal(false);
            }}
            savedLocations={savedLocations}
          />
          <p className="text-xs text-gray-500 dark:text-gray-400 mt-3">
            You currently have {savedLocations.length} saved location{savedLocations.length !== 1 ? 's' : ''}.
          </p>
        </div>
      </Modal>
    </div>
  );
}
