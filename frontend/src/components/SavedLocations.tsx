import React from "react";
import { IconX, IconMapPin } from "@tabler/icons-react";
import type { Location } from "../types/Location";
import type { Weather } from "../types/Weather";
import { weatherCodeMap, isNightTime } from "../types/WeatherCodeMap";

interface SavedLocationsProps {
  locations: Location[];
  weatherData: Map<number, Weather>;
  loadingWeather: Set<number>;
  onRemoveLocation: (locationId: number) => void;
  onLocationClick: (location: Location) => void;
}

function getWeatherIcon(code: string, size = 32, currentTime?: string, sunRise?: string, sunSet?: string) {
  const isNight = currentTime && sunRise && sunSet ? isNightTime(currentTime, sunRise, sunSet) : false;
  return weatherCodeMap[code]?.icon(size, isNight) || null;
}

export default function SavedLocations({
  locations,
  weatherData,
  loadingWeather,
  onRemoveLocation,
  onLocationClick,
}: SavedLocationsProps) {
  if (locations.length === 0) {
    return (
      <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6 text-center">
        <IconMapPin size={48} className="mx-auto text-gray-400 mb-3" />
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">
          No Saved Locations
        </h3>
        <p className="text-gray-600 dark:text-gray-400 text-sm">
          Search and add up to 5 locations to view their weather
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-3">
      <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4">
        Saved Locations ({locations.length}/5)
      </h2>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {locations.map((location) => {
          const weather = weatherData.get(location.id);
          const isLoading = loadingWeather.has(location.id);

          return (
            <div
              key={location.id}
              className="bg-white dark:bg-gray-800 rounded-xl shadow-lg overflow-hidden hover:shadow-xl transition-shadow cursor-pointer relative group"
              onClick={() => onLocationClick(location)}
            >
              {/* Remove button */}
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  onRemoveLocation(location.id);
                }}
                className="absolute top-2 right-2 z-10 bg-red-500 hover:bg-red-600 text-white p-1.5 rounded-full opacity-0 group-hover:opacity-100 transition-opacity"
                aria-label="Remove location"
              >
                <IconX size={16} />
              </button>

              <div className="p-4">
                <div className="flex items-start justify-between mb-3">
                  <div className="flex-1 min-w-0">
                    <h3 className="text-lg font-bold text-gray-900 dark:text-white truncate">
                      {location.name}
                    </h3>
                    <p className="text-sm text-gray-600 dark:text-gray-400 truncate">
                      {[location.admin1, location.country].filter(Boolean).join(", ")}
                    </p>
                  </div>
                </div>

                {isLoading ? (
                  <div className="flex items-center justify-center h-20">
                    <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-blue-500"></div>
                  </div>
                ) : weather ? (
                  <div className="flex items-center justify-between">
                    <div>
                      <div className="text-3xl font-bold text-gray-900 dark:text-white">
                        {Math.round(weather.current.temperature)}°C
                      </div>
                      <div className="text-sm text-gray-600 dark:text-gray-400">
                        {weatherCodeMap[weather.current.weatherCode]?.label || ""}
                      </div>
                    </div>
                    <div className="flex items-center justify-center">
                      {getWeatherIcon(weather.current.weatherCode, 48, weather.current.time, weather.daily[0]?.sunRise, weather.daily[0]?.sunSet)}
                    </div>
                  </div>
                ) : (
                  <div className="text-sm text-gray-500 dark:text-gray-400 text-center py-4">
                    Weather data unavailable
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
