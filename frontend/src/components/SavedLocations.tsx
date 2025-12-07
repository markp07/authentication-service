import React from "react";
import { IconX, IconMapPin, IconGripVertical } from "@tabler/icons-react";
import type { Location } from "../types/Location";
import type { Weather } from "../types/Weather";
import { weatherCodeMap, isNightTime } from "../types/WeatherCodeMap";
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  DragEndEvent,
} from "@dnd-kit/core";
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  useSortable,
  verticalListSortingStrategy,
} from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";

interface SavedLocationsProps {
  locations: Location[];
  weatherData: Map<number, Weather>;
  loadingWeather: Set<number>;
  onRemoveLocation: (locationId: number) => void;
  onLocationClick: (location: Location) => void;
  onReorderLocations: (locationIds: number[]) => void;
}

function getWeatherIcon(code: string, size = 32, currentTime?: string, sunRise?: string, sunSet?: string) {
  const isNight = currentTime && sunRise && sunSet ? isNightTime(currentTime, sunRise, sunSet) : false;
  return weatherCodeMap[code]?.icon(size, isNight) || null;
}

interface SortableLocationItemProps {
  location: Location;
  weather?: Weather;
  isLoading: boolean;
  onRemoveLocation: (locationId: number) => void;
  onLocationClick: (location: Location) => void;
}

function SortableLocationItem({
  location,
  weather,
  isLoading,
  onRemoveLocation,
  onLocationClick,
}: SortableLocationItemProps) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: location.id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  };

  return (
    <div
      ref={setNodeRef}
      style={style}
      className="bg-white dark:bg-gray-800 rounded-xl shadow-lg overflow-hidden hover:shadow-xl transition-shadow relative group"
    >
      {/* Drag handle */}
      <div
        {...attributes}
        {...listeners}
        className="absolute top-2 left-2 z-10 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 cursor-grab active:cursor-grabbing p-1"
        aria-label="Drag to reorder"
      >
        <IconGripVertical size={20} />
      </div>

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

      <div
        className="p-4 cursor-pointer"
        onClick={() => onLocationClick(location)}
      >
        <div className="flex items-start justify-between mb-3 ml-8">
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
}

export default function SavedLocations({
  locations,
  weatherData,
  loadingWeather,
  onRemoveLocation,
  onLocationClick,
  onReorderLocations,
}: SavedLocationsProps) {
  const [items, setItems] = React.useState(locations);

  // Update items when locations prop changes
  React.useEffect(() => {
    setItems(locations);
  }, [locations]);

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 8, // 8px of movement before drag starts
      },
    }),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  );

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;

    if (over && active.id !== over.id) {
      const oldIndex = items.findIndex((item) => item.id === active.id);
      const newIndex = items.findIndex((item) => item.id === over.id);

      const newItems = arrayMove(items, oldIndex, newIndex);
      setItems(newItems);
      
      // Call the callback with the new order of location IDs
      onReorderLocations(newItems.map(item => item.id));
    }
  };

  if (locations.length === 0) {
    return (
      <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6 text-center">
        <IconMapPin size={48} className="mx-auto text-gray-400 mb-3" />
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">
          No Saved Locations
        </h3>
        <p className="text-gray-600 dark:text-gray-400 text-sm">
          Search and add locations to view their weather
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-3">
      <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4">
        Saved Locations ({locations.length})
      </h2>
      <DndContext
        sensors={sensors}
        collisionDetection={closestCenter}
        onDragEnd={handleDragEnd}
      >
        <SortableContext
          items={items.map(loc => loc.id)}
          strategy={verticalListSortingStrategy}
        >
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {items.map((location) => {
              const weather = weatherData.get(location.id);
              const isLoading = loadingWeather.has(location.id);

              return (
                <SortableLocationItem
                  key={location.id}
                  location={location}
                  weather={weather}
                  isLoading={isLoading}
                  onRemoveLocation={onRemoveLocation}
                  onLocationClick={onLocationClick}
                />
              );
            })}
          </div>
        </SortableContext>
      </DndContext>
    </div>
  );
}
