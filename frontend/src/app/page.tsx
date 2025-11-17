"use client";

import React from "react";
import { useRouter } from "next/navigation";
import Modal from "../components/Modal";
import Login from "../components/Login";
import Register from "../components/Register";
import ForgotPassword from "../components/ForgotPassword";
import ResetPassword from "../components/ResetPassword";
import Sidebar from "../components/Sidebar";
import HourlyGraphModal from "../components/HourlyGraphModal";
import LocationSearch from "../components/LocationSearch";
import SavedLocations from "../components/SavedLocations";
import { IconSun, IconWind, IconArrowUp, IconArrowUpLeft, IconArrowUpRight, IconArrowDown, IconArrowDownLeft, IconArrowDownRight, IconArrowRight, IconArrowLeft, IconSearch, IconCurrentLocation, IconX, IconChartHistogram } from "@tabler/icons-react";
import type { Weather } from "../types/Weather";
import type { Location } from "../types/Location";
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

const SAVED_LOCATIONS_KEY = "savedLocations";
const MAX_SAVED_LOCATIONS = 5;

export default function Home() {
  const router = useRouter();
  const [modal, setModal] = React.useState<
    | "login"
    | "register"
    | "forgot"
    | "reset"
    | null
  >("login");
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

  // Load saved locations from localStorage on mount
  React.useEffect(() => {
    if (typeof window !== "undefined" && loggedIn) {
      const saved = localStorage.getItem(SAVED_LOCATIONS_KEY);
      if (saved) {
        try {
          const locations: Location[] = JSON.parse(saved);
          setSavedLocations(locations);
        } catch (e) {
          console.error("Failed to load saved locations:", e);
        }
      }
    }
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

  // Save locations to localStorage
  const saveLocationsToStorage = (locations: Location[]) => {
    if (typeof window !== "undefined") {
      localStorage.setItem(SAVED_LOCATIONS_KEY, JSON.stringify(locations));
    }
  };

  const handleLocationSelect = (location: Location) => {
    if (savedLocations.length >= MAX_SAVED_LOCATIONS) {
      alert(`You can only save up to ${MAX_SAVED_LOCATIONS} locations. Please remove one first.`);
      return;
    }

    if (savedLocations.some(loc => loc.id === location.id)) {
      return; // Already saved
    }

    const newLocations = [...savedLocations, location];
    setSavedLocations(newLocations);
    saveLocationsToStorage(newLocations);
  };

  const handleRemoveLocation = (locationId: number) => {
    const newLocations = savedLocations.filter(loc => loc.id !== locationId);
    setSavedLocations(newLocations);
    saveLocationsToStorage(newLocations);
    setSavedWeatherData(prev => {
      const next = new Map(prev);
      next.delete(locationId);
      return next;
    });
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
    setModal("login");
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
                            {selectedLocationId === null && <IconCurrentLocation size={24} className="text-white" />}
                          </div>
                          <div className="text-5xl sm:text-6xl font-extrabold my-3 sm:my-4">{Math.round(displayWeather.current.temperature)}°C</div>
                          <div className="text-lg sm:text-xl font-medium opacity-90 mb-2 sm:mb-3">{getWeatherLabel(displayWeather.current.weatherCode)}</div>
                          <div className="flex items-center gap-3 sm:gap-4 text-sm opacity-90">
                            <div className="flex items-center gap-1.5 sm:gap-2">
                              <IconWind size={18} />
                              <span>{displayWeather.current.windSpeed} km/h</span>
                              {getWindDirectionIcon(displayWeather.current.windDirection, 18)}
                            </div>
                          </div>
                        </div>
                        <div className="flex items-center justify-center self-stretch">
                          {getWeatherIcon(displayWeather.current.weatherCode, 160)}
                        </div>
                      </div>
                    </div>
                    {/* Horizontal Scrollable Location Bar */}
                    <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-2 sm:p-3">
                      <div className="flex gap-2 overflow-x-auto pb-0 scrollbar-hide" style={{ scrollbarWidth: 'none', msOverflowStyle: 'none' }}>
                        {/* Current Location Card */}
                        <button
                          onClick={() => handleLocationClick(null)}
                          className={`flex-shrink-0 min-w-[120px] p-2 py-1 sm:py-2 rounded-lg transition-all ${
                            selectedLocationId === null
                              ? 'bg-blue-500 text-white'
                              : 'bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600'
                          }`}
                        >
                          <div className="flex items-center gap-1.5 mb-0.5">
                            <span className="text-xs font-semibold">{weather.location}</span>
                            <IconCurrentLocation size={14} />
                          </div>
                          {weather && (
                            <>
                              <div className="flex items-center justify-between mt-0.5">
                                <span className="text-base font-bold">{Math.round(weather.current.temperature)}°C</span>
                                {getWeatherIcon(weather.current.weatherCode, 20)}
                              </div>
                            </>
                          )}
                        </button>

                      {/* Saved Location Cards */}
                      {savedLocations.map((location) => {
                        const locationWeather = savedWeatherData.get(location.id);
                        const isLoading = loadingWeather.has(location.id);
                        const isSelected = selectedLocationId === location.id;

                          return (
                            <div key={location.id} className="flex-shrink-0 min-w-[120px]">
                              <button
                                onClick={() => handleLocationClick(location.id)}
                                className={`relative w-full h-full p-2 py-1 sm:py-2 rounded-lg transition-all ${
                                  isSelected
                                    ? 'bg-blue-500 text-white '
                                    : 'bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600'
                                }`}
                              >
                                <div className="flex items-start justify-between gap-1 mb-0.5">
                                  <div className="text-xs text-left font-semibold truncate flex-1">{location.name}</div>
                                  <button
                                    onClick={(e) => {
                                      e.stopPropagation();
                                      handleRemoveLocation(location.id);
                                      if (selectedLocationId === location.id) {
                                        handleLocationClick(null);
                                      }
                                    }}
                                    className="flex-shrink-0 hover:bg-red-500 hover:text-white p-0.5 rounded transition-colors"
                                    aria-label="Remove location"
                                  >
                                    <IconX size={12} />
                                  </button>
                                </div>
                                {isLoading ? (
                                  <div className="flex items-center justify-center h-10">
                                    <div className="animate-spin rounded-full h-5 w-5 border-t-2 border-b-2 border-current"></div>
                                  </div>
                                ) : locationWeather ? (
                                  <>
                                    <div className="flex items-center justify-between mt-0.5">
                                      <span className="text-base font-bold">{Math.round(locationWeather.current.temperature)}°C</span>
                                      {getWeatherIcon(locationWeather.current.weatherCode, 20)}
                                    </div>
                                  </>
                                ) : (
                                  <div className="text-xs opacity-70">No data</div>
                                )}
                              </button>
                            </div>
                          );
                        })}

                        {/* Add Location Search Button */}
                        <button
                          onClick={() => setShowLocationSearchModal(true)}
                          className="flex-shrink-0 min-w-[120px] p-2 py-1 sm:py-2 rounded-lg transition-all bg-gradient-to-br from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 text-white border-2 border-dashed border-blue-300"
                        >
                          <div className="flex flex-col items-center justify-center h-full gap-1">
                            <IconSearch size={24} />
                            <span className="text-xs font-semibold">Add Location</span>
                          </div>
                        </button>
                      </div>
                    </div>

                    {/* Hourly Forecast Card */}
                    <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-2 sm:p-5 lg:p-6">
                      <div className="flex justify-between items-center mb-2 sm:mb-4">
                        <h3 className="text-lg sm:text-xl font-bold text-gray-900 dark:text-white">Hourly Forecast</h3>
                        <button
                          onClick={() => setShowHourlyGraph(true)}
                          className="px-4 py-1 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium transition-colors text-sm"
                        >
                          <IconChartHistogram size={20} />
                        </button>
                      </div>
                      <div className="flex gap-2 sm:gap-3 overflow-x-auto scrollbar-hide" style={{ scrollbarWidth: 'none', msOverflowStyle: 'none' }}>
                        {displayWeather.hourly.slice(0, 48).map((h, i) => (
                          <div key={i} className="flex flex-col items-center min-w-[65px] sm:min-w-[80px] p-2 sm:p-3 rounded-lg bg-gray-50 dark:bg-gray-700 border border-gray-200 dark:border-gray-600">
                            <div className="text-[10px] sm:text-xs font-medium text-gray-600 dark:text-gray-400 mb-1 sm:mb-2">
                              {i === 0 ? "Now" : new Date(h.time).toLocaleTimeString([], { hour: "2-digit", hour12: false })}
                            </div>
                            <div className="mb-1 sm:mb-2">{getWeatherIcon(h.weatherCode, 32)}</div>
                            <div className="font-bold text-base sm:text-lg text-gray-900 dark:text-white">{Math.round(h.temperature)}°</div>
                            <div className="text-[10px] sm:text-xs text-blue-600 dark:text-blue-400 mt-0.5 sm:mt-1">{h.precipitationProbability}%</div>
                            <div className="text-[10px] sm:text-xs text-gray-500 dark:text-gray-400">{h.precipitation.toFixed(1)}mm</div>
                            <div className="flex items-center gap-0.5 sm:gap-1 text-[10px] sm:text-xs text-gray-600 dark:text-gray-400 mt-0.5 sm:mt-1">
                              <span>{h.windSpeed}km/h</span>
                              {getWindDirectionIcon(h.windDirection, 12)}
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>

                    {/* 14-Day Forecast Card */}
                    <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-2 sm:p-5 lg:p-6">
                      <h3 className="text-lg sm:text-xl font-bold text-gray-900 dark:text-white mb-3 sm:mb-4">14-Day Forecast</h3>
                      <div className="space-y-1 sm:space-y-2">
                        {displayWeather.daily.slice(0, 14).map((d, i) => (
                          <div key={i} className="flex items-center gap-2 sm:gap-3 p-2 sm:p-3 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors">
                            <div className="flex-1 min-w-0 text-xs sm:text-sm font-medium text-gray-700 dark:text-gray-300">
                              {i === 0 ? "Today" : new Date(d.time).toLocaleDateString("en-GB", { weekday: "short" })}
                            </div>
                            <div className="flex items-center justify-center w-8 sm:w-10 flex-shrink-0">
                              {getWeatherIcon(d.weatherCode, 28)}
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
                        ))}
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

      {/* Modals */}
      <Modal open={!loggedIn && modal === "login"} onClose={closeModal}>
        <Login
          onSuccess={() => {
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

      {/* Hourly Graph Modal */}
      {displayWeather && (
        <HourlyGraphModal
          open={showHourlyGraph}
          onClose={() => setShowHourlyGraph(false)}
          hourlyData={displayWeather.hourly}
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
            Save up to {MAX_SAVED_LOCATIONS} locations. You currently have {savedLocations.length} saved.
          </p>
        </div>
      </Modal>
    </div>
  );
}
