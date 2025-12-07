import React from "react";
import { Crosshair, PencilSquare } from "react-bootstrap-icons";
import type { Location } from "../types/Location";
import type { Weather } from "../types/Weather";
import { weatherCodeMap, isNightTime } from "../types/WeatherCodeMap";

interface LocationBarProps {
  currentLocationWeather: Weather | null;
  savedLocations: Location[];
  savedWeatherData: Map<number, Weather>;
  loadingWeather: Set<number>;
  selectedLocationId: number | null;
  onLocationClick: (locationId: number | null) => void;
  onEditClick: () => void;
}

function getWeatherIcon(code: string, size = 32, currentTime?: string, sunRise?: string, sunSet?: string) {
  const isNight = currentTime && sunRise && sunSet ? isNightTime(currentTime, sunRise, sunSet) : false;
  return weatherCodeMap[code]?.icon(size, isNight) || null;
}

interface LocationCardProps {
  location: Location;
  locationWeather?: Weather;
  isLoading: boolean;
  isSelected: boolean;
  onLocationClick: (locationId: number) => void;
}

function LocationCard({
  location,
  locationWeather,
  isLoading,
  isSelected,
  onLocationClick,
}: LocationCardProps) {
  return (
    <button
      onClick={() => onLocationClick(location.id)}
      className={`flex-shrink-0 w-[120px] p-2 py-1 sm:py-2 rounded-lg transition-all ${
        isSelected
          ? 'bg-blue-500 text-white '
          : 'bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600'
      }`}
    >
      <div className="flex items-start justify-between gap-1 mb-0.5">
        <div className="text-xs text-left font-semibold truncate flex-1">{location.name}</div>
      </div>
      {isLoading ? (
        <div className="flex items-center justify-center h-10">
          <div className="animate-spin rounded-full h-5 w-5 border-t-2 border-b-2 border-current"></div>
        </div>
      ) : locationWeather ? (
        <>
          <div className="flex items-center justify-between mt-0.5">
            <span className="text-base font-bold">{Math.round(locationWeather.current.temperature)}°C</span>
            {getWeatherIcon(locationWeather.current.weatherCode, 20, locationWeather.current.time, locationWeather.daily[0]?.sunRise, locationWeather.daily[0]?.sunSet)}
          </div>
        </>
      ) : (
        <div className="text-xs opacity-70">No data</div>
      )}
    </button>
  );
}

export default function LocationBar({
  currentLocationWeather,
  savedLocations,
  savedWeatherData,
  loadingWeather,
  selectedLocationId,
  onLocationClick,
  onEditClick,
}: LocationBarProps) {
  return (
    <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-2 sm:p-3">
      <div className="flex gap-2 overflow-x-auto overflow-y-hidden pb-0 scrollbar-hide" style={{ scrollbarWidth: 'none', msOverflowStyle: 'none' }}>
        {/* Current Location Card */}
        <button
          onClick={() => onLocationClick(null)}
          className={`flex-shrink-0 w-[120px] p-2 py-1 sm:py-2 rounded-lg transition-all ${
            selectedLocationId === null
              ? 'bg-blue-500 text-white'
              : 'bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600'
          }`}
        >
          <div className="flex items-center gap-1.5 mb-0.5">
            <span className="text-xs font-semibold truncate flex-1">{currentLocationWeather?.location}</span>
            <Crosshair size={14} className="flex-shrink-0" />
          </div>
          {currentLocationWeather && (
            <>
              <div className="flex items-center justify-between mt-0.5">
                <span className="text-base font-bold">{Math.round(currentLocationWeather.current.temperature)}°C</span>
                {getWeatherIcon(currentLocationWeather.current.weatherCode, 20, currentLocationWeather.current.time, currentLocationWeather.daily[0]?.sunRise, currentLocationWeather.daily[0]?.sunSet)}
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
            <LocationCard
              key={location.id}
              location={location}
              locationWeather={locationWeather}
              isLoading={isLoading}
              isSelected={isSelected}
              onLocationClick={onLocationClick}
            />
          );
        })}

        {/* Edit Button */}
        <button
          onClick={onEditClick}
          className="flex-shrink-0 w-[120px] p-2 py-1 sm:py-2 rounded-lg transition-all bg-gradient-to-br from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 text-white border-2 border-dashed border-blue-300"
        >
          <div className="flex flex-col items-center justify-center h-full gap-1">
            <PencilSquare size={16} />
            <span className="text-xs font-semibold">Edit</span>
          </div>
        </button>
      </div>
    </div>
  );
}
