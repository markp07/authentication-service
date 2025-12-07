import React from "react";
import { IconTemperature, IconDroplet, IconWind, IconArrowUp, IconArrowUpRight, IconArrowRight, IconArrowDownRight, IconArrowDown, IconArrowDownLeft, IconArrowLeft, IconArrowUpLeft, IconChevronLeft, IconChevronRight } from "@tabler/icons-react";
import type { Hourly } from "../types/Hourly";
import type { Daily } from "../types/Daily";
import { weatherCodeMap, isNightTime } from "../types/WeatherCodeMap";

interface HourlyGraphModalProps {
  open: boolean;
  onClose: () => void;
  hourlyData: Hourly[];
  dailyData: Daily[];
}

type DataType = "temperature" | "precipitation" | "wind";

// Screen width breakpoint for responsive hours per page
const SMALL_SCREEN_BREAKPOINT = 500;
const HOURS_PER_PAGE_SMALL = 18;
const HOURS_PER_PAGE_LARGE = 24;

export default function HourlyGraphModal({
  open,
  onClose,
  hourlyData,
  dailyData,
}: HourlyGraphModalProps) {
  const [dataType, setDataType] = React.useState<DataType>("temperature");
  const [page, setPage] = React.useState(0); // Page index (18 or 24 hours per page based on screen size)
  const [containerWidth, setContainerWidth] = React.useState(800);
  const containerRef = React.useRef<HTMLDivElement>(null);

  // Update container width on resize
  React.useEffect(() => {
    if (!open) return;
    
    const updateWidth = () => {
      if (containerRef.current) {
        const width = containerRef.current.clientWidth;
        // Reduced padding subtraction for mobile (8px vs 32px)
        const paddingSubtract = width < SMALL_SCREEN_BREAKPOINT ? 8 : 32;
        setContainerWidth(Math.max(300, width - paddingSubtract));
      }
    };

    updateWidth();
    window.addEventListener('resize', updateWidth);
    return () => window.removeEventListener('resize', updateWidth);
  }, [open]);

  // Reset page when transitioning between small/large screen to avoid out-of-bounds
  // Note: `page` is intentionally not in dependencies - we only check/reset when screen size or data length changes
  React.useEffect(() => {
    const hoursPerPage = containerWidth < SMALL_SCREEN_BREAKPOINT ? HOURS_PER_PAGE_SMALL : HOURS_PER_PAGE_LARGE;
    const maxPage = Math.ceil(hourlyData.length / hoursPerPage) - 1;
    if (page > maxPage) {
      setPage(Math.max(0, maxPage));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [containerWidth, hourlyData.length]);

  // Create a memoized map of dates to daily data for efficient lookup
  const dailyDataMap = React.useMemo(() => {
    const map = new Map<string, Daily>();
    dailyData.forEach(d => {
      map.set(new Date(d.time).toDateString(), d);
    });
    return map;
  }, [dailyData]);

  if (!open) return null;

  // Determine hours per page based on screen size
  const hoursPerPage = containerWidth < SMALL_SCREEN_BREAKPOINT ? HOURS_PER_PAGE_SMALL : HOURS_PER_PAGE_LARGE;
  const startIndex = page * hoursPerPage;
  const endIndex = startIndex + hoursPerPage;
  const displayData = hourlyData.slice(startIndex, endIndex);

  // Get values based on selected data type
  const getValues = (data: Hourly[]): number[] => {
    switch (dataType) {
      case "temperature":
        return data.map((h) => h.temperature);
      case "precipitation":
        return data.map((h) => h.precipitation);
      case "wind":
        return data.map((h) => h.windSpeed);
    }
  };

  const values = getValues(displayData);
  const minValue = dataType === "precipitation" ? 0 : Math.min(...values); // For precipitation, always start from 0
  const maxValue = Math.max(...values);
  const range = maxValue - minValue || 1;

  // Chart dimensions - now responsive
  const width = containerWidth;
  const height = Math.min(300, Math.max(200, width * 0.4)); // Responsive height with limits
  const padding = { 
    top: 20, 
    right: width < SMALL_SCREEN_BREAKPOINT ? 10 : 20, 
    bottom: 40, 
    left: width < SMALL_SCREEN_BREAKPOINT ? 35 : 50 
  };
  const chartWidth = width - padding.left - padding.right;
  const chartHeight = height - padding.top - padding.bottom;

  // Create SVG path for line chart
  const createPath = () => {
    if (values.length === 0) return "";
    
    const points = values.map((value, index) => {
      const x = padding.left + (index / (values.length - 1)) * chartWidth;
      const y = padding.top + chartHeight - ((value - minValue) / range) * chartHeight;
      return `${x},${y}`;
    });
    
    return `M ${points.join(" L ")}`;
  };

  const getLabel = () => {
    switch (dataType) {
      case "temperature":
        return "Temperature (°C)";
      case "precipitation":
        return "Precipitation (mm)";
      case "wind":
        return "Wind Speed (km/h)";
    }
  };

  const getUnit = () => {
    switch (dataType) {
      case "temperature":
        return "°C";
      case "precipitation":
        return "mm";
      case "wind":
        return "km/h";
    }
  };

  // Get wind direction icon based on direction string
  const getWindDirectionIcon = (direction: string, size: number = 16) => {
    const iconMap: { [key: string]: React.ComponentType<{ size: number; className?: string }> } = {
      N: IconArrowDown,
      NE: IconArrowDownLeft,
      E: IconArrowLeft,
      SE: IconArrowUpLeft,
      S: IconArrowUp,
      SW: IconArrowUpRight,
      W: IconArrowRight,
      NW: IconArrowDownRight,
    };
    const IconComponent = iconMap[direction] || IconArrowUp;
    return <IconComponent size={size} className="text-blue-600 dark:text-blue-400" />;
  };

  // Get weather icon based on weather code and time
  const getWeatherIcon = (hourly: Hourly, size = 24) => {
    const hourDate = new Date(hourly.time).toDateString();
    const dailyInfo = dailyDataMap.get(hourDate);
    const isNight = dailyInfo ? isNightTime(hourly.time, dailyInfo.sunRise, dailyInfo.sunSet) : false;
    const weatherIcon = weatherCodeMap[hourly.weatherCode];
    if (weatherIcon) {
      return weatherIcon.icon(size, isNight);
    }
    // Fallback to CLEAR_SKY if available, otherwise return null
    return weatherCodeMap.CLEAR_SKY?.icon(size, isNight) || null;
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-2 sm:p-4">
      <div className="bg-white dark:bg-gray-900 rounded-xl shadow-2xl w-full max-w-5xl max-h-[95vh] sm:max-h-[90vh] overflow-hidden flex flex-col relative">
        <button
          className="absolute top-2 right-2 sm:top-4 sm:right-4 text-gray-500 hover:text-gray-900 dark:hover:text-white text-2xl sm:text-3xl font-bold focus:outline-none z-20 bg-white/80 dark:bg-gray-900/80 backdrop-blur-sm rounded-full w-8 h-8 sm:w-10 sm:h-10 flex items-center justify-center hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
          onClick={onClose}
          aria-label="Close modal"
        >
          ×
        </button>
        
        <div className="p-3 sm:p-4 md:p-6 overflow-y-auto">
          {/* Title */}
          <h2 className="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white mb-3 sm:mb-4 text-center pr-8">
            24-Hour Forecast
          </h2>

          {/* Data Type Selector */}
          <div className="flex gap-2 sm:gap-3 mb-4 sm:mb-6 flex-wrap justify-center">
            <button
              onClick={() => setDataType("temperature")}
              className={`flex items-center justify-center gap-1.5 sm:gap-2 p-3 sm:px-5 sm:py-3 rounded-lg font-semibold text-sm sm:text-base transition-all ${
                dataType === "temperature"
                  ? "bg-blue-600 text-white shadow-lg scale-105"
                  : "bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-300 dark:hover:bg-gray-600"
              }`}
              title="Temperature"
            >
              <IconTemperature size={24} className="sm:w-6 sm:h-6" />
              <span className="hidden sm:inline">Temperature</span>
            </button>
            <button
              onClick={() => setDataType("precipitation")}
              className={`flex items-center justify-center gap-1.5 sm:gap-2 p-3 sm:px-5 sm:py-3 rounded-lg font-semibold text-sm sm:text-base transition-all ${
                dataType === "precipitation"
                  ? "bg-blue-600 text-white shadow-lg scale-105"
                  : "bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-300 dark:hover:bg-gray-600"
              }`}
              title="Precipitation"
            >
              <IconDroplet size={24} className="sm:w-6 sm:h-6" />
              <span className="hidden sm:inline">Precipitation</span>
            </button>
            <button
              onClick={() => setDataType("wind")}
              className={`flex items-center justify-center gap-1.5 sm:gap-2 p-3 sm:px-5 sm:py-3 rounded-lg font-semibold text-sm sm:text-base transition-all ${
                dataType === "wind"
                  ? "bg-blue-600 text-white shadow-lg scale-105"
                  : "bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-300 dark:hover:bg-gray-600"
              }`}
              title="Wind Speed"
            >
              <IconWind size={24} className="sm:w-6 sm:h-6" />
              <span className="hidden sm:inline">Wind Speed</span>
            </button>
          </div>

          {/* Chart */}
          <div ref={containerRef} className="bg-gradient-to-br from-gray-50 to-gray-100 dark:from-gray-800 dark:to-gray-900 rounded-xl p-1 sm:p-4 mb-4 sm:mb-6 shadow-inner">
            <svg
              width={width}
              height={height}
              className="mx-auto w-full"
              viewBox={`0 0 ${width} ${height}`}
              preserveAspectRatio="xMidYMid meet"
            >
              {/* Grid lines */}
              {[0, 1, 2, 3, 4].map((i) => {
                const y = padding.top + (i / 4) * chartHeight;
                const value = maxValue - (i / 4) * range;
                return (
                  <g key={i}>
                    <line
                      x1={padding.left}
                      y1={y}
                      x2={width - padding.right}
                      y2={y}
                      stroke="currentColor"
                      strokeWidth="1"
                      className="text-gray-300 dark:text-gray-600"
                      strokeDasharray="4"
                    />
                    <text
                      x={padding.left - 5}
                      y={y + 4}
                      textAnchor="end"
                      className="text-[10px] sm:text-xs fill-gray-600 dark:fill-gray-400"
                      style={{ fontSize: width < SMALL_SCREEN_BREAKPOINT ? '9px' : '12px' }}
                    >
                      {value.toFixed(width < SMALL_SCREEN_BREAKPOINT ? 0 : 1)}
                    </text>
                  </g>
                );
              })}

              {/* X-axis labels */}
              {displayData.map((h, i) => {
                // Show fewer labels on mobile
                const skipInterval = width < SMALL_SCREEN_BREAKPOINT ? 4 : 3;
                if (i % skipInterval !== 0) return null;
                // For bar chart, center labels under bars; for line chart, use data point positions
                const barSpacing = chartWidth / values.length;
                const x = dataType === "precipitation" 
                  ? padding.left + (i * barSpacing) + barSpacing / 2
                  : padding.left + (i / (values.length - 1)) * chartWidth;
                const time = new Date(h.time);
                const label = time.getHours().toString().padStart(2, "0") + ":00";
                return (
                  <text
                    key={i}
                    x={x}
                    y={height - padding.bottom + 20}
                    textAnchor="middle"
                    className="text-[10px] sm:text-xs fill-gray-600 dark:fill-gray-400"
                    style={{ fontSize: width < SMALL_SCREEN_BREAKPOINT ? '9px' : '12px' }}
                  >
                    {label}
                  </text>
                );
              })}

              {/* Gradient definition for line and bars */}
              <defs>
                <linearGradient id="lineGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                  <stop offset="0%" stopColor="rgb(59, 130, 246)" stopOpacity="0.8" />
                  <stop offset="100%" stopColor="rgb(37, 99, 235)" stopOpacity="1" />
                </linearGradient>
                <linearGradient id="areaGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                  <stop offset="0%" stopColor="rgb(59, 130, 246)" stopOpacity="0.3" />
                  <stop offset="100%" stopColor="rgb(59, 130, 246)" stopOpacity="0.05" />
                </linearGradient>
                <linearGradient id="barGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                  <stop offset="0%" stopColor="rgb(59, 130, 246)" stopOpacity="0.9" />
                  <stop offset="100%" stopColor="rgb(37, 99, 235)" stopOpacity="1" />
                </linearGradient>
              </defs>

              {/* Render bar chart for precipitation */}
              {dataType === "precipitation" && values.map((value, index) => {
                const barWidth = Math.max(4, (chartWidth / values.length) * 0.7);
                const barSpacing = chartWidth / values.length;
                const x = padding.left + (index * barSpacing) + (barSpacing - barWidth) / 2;
                const barHeight = maxValue > 0 ? (value / maxValue) * chartHeight : 0;
                const y = padding.top + chartHeight - barHeight;
                return (
                  <g key={index}>
                    <rect
                      x={x}
                      y={y}
                      width={barWidth}
                      height={barHeight}
                      fill="url(#barGradient)"
                      rx={width < SMALL_SCREEN_BREAKPOINT ? 1 : 2}
                      ry={width < SMALL_SCREEN_BREAKPOINT ? 1 : 2}
                      className="cursor-pointer hover:opacity-80 transition-opacity"
                    >
                      <title>{`${new Date(displayData[index].time).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}: ${value.toFixed(1)} ${getUnit()}`}</title>
                    </rect>
                  </g>
                );
              })}

              {/* Render line chart for temperature and wind */}
              {dataType !== "precipitation" && (
                <>
                  {/* Area under the line */}
                  <path
                    d={`${createPath()} L ${padding.left + chartWidth},${padding.top + chartHeight} L ${padding.left},${padding.top + chartHeight} Z`}
                    fill="url(#areaGradient)"
                  />

                  {/* Line chart */}
                  <path
                    d={createPath()}
                    fill="none"
                    stroke="url(#lineGradient)"
                    strokeWidth={width < SMALL_SCREEN_BREAKPOINT ? "2.5" : "3"}
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  />

                  {/* Data points */}
                  {values.map((value, index) => {
                    const x = padding.left + (index / (values.length - 1)) * chartWidth;
                    const y = padding.top + chartHeight - ((value - minValue) / range) * chartHeight;
                    const pointRadius = width < SMALL_SCREEN_BREAKPOINT ? 3 : 4;
                    return (
                      <g key={index}>
                        <circle
                          cx={x}
                          cy={y}
                          r={pointRadius}
                          fill="white"
                          stroke="rgb(37, 99, 235)"
                          strokeWidth="2"
                          className="cursor-pointer transition-all"
                        />
                        <circle
                          cx={x}
                          cy={y}
                          r={pointRadius + 8}
                          fill="transparent"
                          className="cursor-pointer"
                        >
                          <title>{`${new Date(displayData[index].time).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}: ${value.toFixed(1)} ${getUnit()}`}</title>
                        </circle>
                      </g>
                    );
                  })}
                </>
              )}
            </svg>

            {/* Weather Code Icons - shown for temperature and precipitation graphs */}
            {(dataType === "temperature" || dataType === "precipitation") && (
              <div className="mt-1 px-1 sm:px-2">
                <div className="flex justify-between items-center" style={{ marginLeft: `${(padding.left / width) * 100}%`, marginRight: `${(padding.right / width) * 100}%` }}>
                  {displayData.map((h, index) => {
                    const iconSize = width < SMALL_SCREEN_BREAKPOINT ? 20 : 24;
                    return (
                      <div 
                        key={index} 
                        className="flex flex-col items-center" 
                        style={{ width: `${100 / displayData.length}%` }}
                        title={weatherCodeMap[h.weatherCode]?.label || h.weatherCode}
                      >
                        {getWeatherIcon(h, iconSize)}
                      </div>
                    );
                  })}
                </div>
              </div>
            )}

            {/* Wind Direction Indicators - shown only when wind data type is selected */}
            {dataType === "wind" && (
              <div className="mt-2 px-1 sm:px-2">
                <div className="flex justify-between items-center" style={{ marginLeft: `${(padding.left / width) * 100}%`, marginRight: `${(padding.right / width) * 100}%` }}>
                  {displayData.map((h, index) => {
                    const iconSize = width < SMALL_SCREEN_BREAKPOINT ? 14 : 18;
                    return (
                      <div 
                        key={index} 
                        className="flex flex-col items-center" 
                        style={{ width: `${100 / displayData.length}%` }}
                        title={`${h.windDirection} - ${h.windSpeed} km/h`}
                      >
                        {getWindDirectionIcon(h.windDirection, iconSize)}
                      </div>
                    );
                  })}
                </div>
              </div>
            )}
          </div>

          {/* Page Navigation */}
          <div className="flex justify-center gap-2 sm:gap-3">
            <button
              onClick={() => setPage(Math.max(0, page - 1))}
              disabled={page === 0}
              className={`flex items-center gap-1 sm:gap-2 px-3 sm:px-5 py-2 sm:py-2.5 rounded-lg font-medium text-sm sm:text-base transition-all ${
                page === 0
                  ? "bg-gray-200 dark:bg-gray-700 text-gray-400 dark:text-gray-500"
                  : "bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-300 dark:hover:bg-gray-600 hover:shadow-md"
              } disabled:opacity-50 disabled:cursor-not-allowed`}
            >
              <IconChevronLeft size={18} />
              <span>Previous</span>
            </button>
            <button
              onClick={() => setPage(page + 1)}
              disabled={hourlyData.length <= (page + 1) * hoursPerPage}
              className={`flex items-center gap-1 sm:gap-2 px-3 sm:px-5 py-2 sm:py-2.5 rounded-lg font-medium text-sm sm:text-base transition-all ${
                hourlyData.length <= (page + 1) * hoursPerPage
                  ? "bg-gray-200 dark:bg-gray-700 text-gray-400 dark:text-gray-500"
                  : "bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-300 dark:hover:bg-gray-600 hover:shadow-md"
              } disabled:opacity-50 disabled:cursor-not-allowed`}
            >
              <span>Next</span>
              <IconChevronRight size={18} />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
