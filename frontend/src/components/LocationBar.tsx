import React from "react";
import { Crosshair, Search, XLg } from "react-bootstrap-icons";
import type { Location } from "../types/Location";
import type { Weather } from "../types/Weather";
import { weatherCodeMap, isNightTime } from "../types/WeatherCodeMap";
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  TouchSensor,
  useSensor,
  useSensors,
  DragEndEvent,
} from "@dnd-kit/core";
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  useSortable,
  horizontalListSortingStrategy,
} from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import { IconGripVertical } from "@tabler/icons-react";

interface LocationBarProps {
  currentLocationWeather: Weather | null;
  savedLocations: Location[];
  savedWeatherData: Map<number, Weather>;
  loadingWeather: Set<number>;
  selectedLocationId: number | null;
  onLocationClick: (locationId: number | null) => void;
  onRemoveLocation: (locationId: number) => void;
  onReorderLocations: (locationIds: number[]) => void;
  onAddLocationClick: () => void;
}

function getWeatherIcon(code: string, size = 32, currentTime?: string, sunRise?: string, sunSet?: string) {
  const isNight = currentTime && sunRise && sunSet ? isNightTime(currentTime, sunRise, sunSet) : false;
  return weatherCodeMap[code]?.icon(size, isNight) || null;
}

interface SortableLocationCardProps {
  location: Location;
  locationWeather?: Weather;
  isLoading: boolean;
  isSelected: boolean;
  onLocationClick: (locationId: number) => void;
  onRemoveLocation: (locationId: number) => void;
}

function SortableLocationCard({
  location,
  locationWeather,
  isLoading,
  isSelected,
  onLocationClick,
  onRemoveLocation,
}: SortableLocationCardProps) {
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
      className="flex-shrink-0 min-w-[120px] relative"
    >
      {/* Drag handle */}
      <div
        {...attributes}
        {...listeners}
        className="absolute top-0 left-0 z-20 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 cursor-grab active:cursor-grabbing p-1"
        onClick={(e) => e.stopPropagation()}
      >
        <IconGripVertical size={12} />
      </div>

      <button
        onClick={() => onLocationClick(location.id)}
        className={`w-full h-full p-2 py-1 sm:py-2 pl-6 rounded-lg transition-all ${
          isSelected
            ? 'bg-blue-500 text-white '
            : 'bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600'
        }`}
      >
        <div className="flex items-start justify-between gap-1 mb-0.5">
          <div className="text-xs text-left font-semibold truncate flex-1 pr-4">{location.name}</div>
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
      <button
        onClick={(e) => {
          e.stopPropagation();
          onRemoveLocation(location.id);
        }}
        className="absolute top-1 right-1 flex-shrink-0 hover:bg-red-500 hover:text-white text-gray-600 dark:text-gray-400 p-0.5 rounded transition-colors z-10"
        aria-label="Remove location"
      >
        <XLg size={12} />
      </button>
    </div>
  );
}

export default function LocationBar({
  currentLocationWeather,
  savedLocations,
  savedWeatherData,
  loadingWeather,
  selectedLocationId,
  onLocationClick,
  onRemoveLocation,
  onReorderLocations,
  onAddLocationClick,
}: LocationBarProps) {
  const [items, setItems] = React.useState(savedLocations);

  // Update items when locations prop changes
  React.useEffect(() => {
    setItems(savedLocations);
  }, [savedLocations]);

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 8, // 8px of movement before drag starts
      },
    }),
    useSensor(TouchSensor, {
      activationConstraint: {
        delay: 200,
        tolerance: 8,
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

  return (
    <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-2 sm:p-3">
      <div className="flex gap-2 overflow-x-auto pb-0 scrollbar-hide" style={{ scrollbarWidth: 'none', msOverflowStyle: 'none' }}>
        {/* Current Location Card - Not draggable */}
        <button
          onClick={() => onLocationClick(null)}
          className={`flex-shrink-0 min-w-[120px] p-2 py-1 sm:py-2 rounded-lg transition-all ${
            selectedLocationId === null
              ? 'bg-blue-500 text-white'
              : 'bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600'
          }`}
        >
          <div className="flex items-center gap-1.5 mb-0.5">
            <span className="text-xs font-semibold">{currentLocationWeather?.location}</span>
            <Crosshair size={14} />
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

        {/* Saved Location Cards - Draggable */}
        <DndContext
          sensors={sensors}
          collisionDetection={closestCenter}
          onDragEnd={handleDragEnd}
        >
          <SortableContext
            items={items.map(loc => loc.id)}
            strategy={horizontalListSortingStrategy}
          >
            {items.map((location) => {
              const locationWeather = savedWeatherData.get(location.id);
              const isLoading = loadingWeather.has(location.id);
              const isSelected = selectedLocationId === location.id;

              return (
                <SortableLocationCard
                  key={location.id}
                  location={location}
                  locationWeather={locationWeather}
                  isLoading={isLoading}
                  isSelected={isSelected}
                  onLocationClick={onLocationClick}
                  onRemoveLocation={onRemoveLocation}
                />
              );
            })}
          </SortableContext>
        </DndContext>

        {/* Add Location Search Button - Not draggable */}
        <button
          onClick={onAddLocationClick}
          className="flex-shrink-0 min-w-[120px] p-2 py-1 sm:py-2 rounded-lg transition-all bg-gradient-to-br from-blue-500 to-blue-600 hover:from-blue-600 hover:to-blue-700 text-white border-2 border-dashed border-blue-300"
        >
          <div className="flex flex-col items-center justify-center h-full gap-1">
            <Search size={16} />
            <span className="text-xs font-semibold">Add Location</span>
          </div>
        </button>
      </div>
    </div>
  );
}
