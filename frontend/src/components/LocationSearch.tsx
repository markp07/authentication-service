import React from "react";
import { IconSearch, IconX, IconPlus, IconMapPin } from "@tabler/icons-react";
import type { Location } from "../types/Location";

interface LocationSearchProps {
  weatherApiBase: string;
  onLocationSelect: (location: Location) => void;
  savedLocations: Location[];
}

export default function LocationSearch({ weatherApiBase, onLocationSelect, savedLocations }: LocationSearchProps) {
  const [searchQuery, setSearchQuery] = React.useState("");
  const [searchResults, setSearchResults] = React.useState<Location[]>([]);
  const [isSearching, setIsSearching] = React.useState(false);
  const [showResults, setShowResults] = React.useState(false);
  const searchTimeoutRef = React.useRef<NodeJS.Timeout | null>(null);

  const searchLocations = React.useCallback(async (query: string) => {
    if (!query.trim()) {
      setSearchResults([]);
      setShowResults(false);
      return;
    }

    setIsSearching(true);
    setShowResults(true);

    try {
      const res = await fetch(
        `${weatherApiBase}/api/weather/v1/search?name=${encodeURIComponent(query)}`,
        { credentials: "include" }
      );

      if (res.ok) {
        const data: Location[] = await res.json();
        setSearchResults(data);
      } else {
        setSearchResults([]);
      }
    } catch (error) {
      console.error("Error searching locations:", error);
      setSearchResults([]);
    } finally {
      setIsSearching(false);
    }
  }, [weatherApiBase]);

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const query = e.target.value;
    setSearchQuery(query);

    // Debounce the search
    if (searchTimeoutRef.current) {
      clearTimeout(searchTimeoutRef.current);
    }

    searchTimeoutRef.current = setTimeout(() => {
      searchLocations(query);
    }, 300);
  };

  const handleLocationClick = (location: Location) => {
    onLocationSelect(location);
    setSearchQuery("");
    setSearchResults([]);
    setShowResults(false);
  };

  const isLocationSaved = (locationId: number) => {
    return savedLocations.some(loc => loc.id === locationId);
  };

  const getLocationDisplayName = (location: Location) => {
    const parts = [location.name];
    if (location.admin1) parts.push(location.admin1);
    if (location.country) parts.push(location.country);
    return parts.join(", ");
  };

  return (
    <div className="relative">
      <div className="relative">
        <input
          type="text"
          value={searchQuery}
          onChange={handleSearchChange}
          placeholder="Search for a city..."
          className="w-full px-4 py-3 pl-11 pr-10 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 dark:text-white"
          onFocus={() => searchQuery && setShowResults(true)}
        />
        <IconSearch className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={20} />
        {searchQuery && (
          <button
            onClick={() => {
              setSearchQuery("");
              setSearchResults([]);
              setShowResults(false);
            }}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
          >
            <IconX size={20} />
          </button>
        )}
      </div>

      {/* Search Results Dropdown */}
      {showResults && (
        <div className="absolute z-50 w-full mt-2 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 rounded-lg shadow-xl max-h-96 overflow-y-auto">
          {isSearching ? (
            <div className="p-4 text-center text-gray-500 dark:text-gray-400">
              <div className="animate-spin rounded-full h-6 w-6 border-t-2 border-b-2 border-blue-500 mx-auto"></div>
              <p className="mt-2 text-sm">Searching...</p>
            </div>
          ) : searchResults.length > 0 ? (
            <ul className="py-2">
              {searchResults.map((location) => {
                const isSaved = isLocationSaved(location.id);
                return (
                  <li key={location.id}>
                    <button
                      onClick={() => !isSaved && handleLocationClick(location)}
                      disabled={isSaved}
                      className={`w-full px-4 py-3 text-left hover:bg-gray-50 dark:hover:bg-gray-700 flex items-center justify-between transition-colors ${
                        isSaved ? "opacity-50 cursor-not-allowed" : ""
                      }`}
                    >
                      <div className="flex items-center gap-3 flex-1 min-w-0">
                        <IconMapPin size={18} className="text-blue-500 flex-shrink-0" />
                        <div className="flex-1 min-w-0">
                          <div className="font-medium text-gray-900 dark:text-white truncate">
                            {getLocationDisplayName(location)}
                          </div>
                          {location.countryCode && (
                            <div className="text-xs text-gray-500 dark:text-gray-400">
                              {location.countryCode}
                            </div>
                          )}
                        </div>
                      </div>
                      {isSaved ? (
                        <span className="text-xs text-gray-500 dark:text-gray-400 flex-shrink-0">Saved</span>
                      ) : (
                        <IconPlus size={18} className="text-gray-400 flex-shrink-0" />
                      )}
                    </button>
                  </li>
                );
              })}
            </ul>
          ) : (
            <div className="p-4 text-center text-gray-500 dark:text-gray-400 text-sm">
              {searchQuery ? "No locations found" : "Start typing to search"}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
