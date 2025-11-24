import React from "react";
import { IconTemperature, IconDroplet, IconWind } from "@tabler/icons-react";
import type { Hourly } from "../types/Hourly";

interface HourlyGraphModalProps {
  open: boolean;
  onClose: () => void;
  hourlyData: Hourly[];
}

type DataType = "temperature" | "precipitation" | "wind";

export default function HourlyGraphModal({
  open,
  onClose,
  hourlyData,
}: HourlyGraphModalProps) {
  const [dataType, setDataType] = React.useState<DataType>("temperature");
  const [page, setPage] = React.useState(0); // 0 = first 24 hours, 1 = next 24 hours
  const [containerWidth, setContainerWidth] = React.useState(800);
  const containerRef = React.useRef<HTMLDivElement>(null);

  // Update container width on resize
  React.useEffect(() => {
    if (!open) return;
    
    const updateWidth = () => {
      if (containerRef.current) {
        const width = containerRef.current.clientWidth;
        setContainerWidth(Math.max(300, width - 32)); // Minimum 300px, subtract padding
      }
    };

    updateWidth();
    window.addEventListener('resize', updateWidth);
    return () => window.removeEventListener('resize', updateWidth);
  }, [open]);

  if (!open) return null;

  const startIndex = page * 24;
  const endIndex = startIndex + 24;
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
  const minValue = Math.min(...values);
  const maxValue = Math.max(...values);
  const range = maxValue - minValue || 1;

  // Chart dimensions - now responsive
  const width = containerWidth;
  const height = Math.min(300, Math.max(200, width * 0.4)); // Responsive height with limits
  const padding = { 
    top: 20, 
    right: width < 500 ? 10 : 20, 
    bottom: 40, 
    left: width < 500 ? 35 : 50 
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
              className={`flex items-center gap-1.5 sm:gap-2 px-3 sm:px-5 py-2 sm:py-3 rounded-lg font-semibold text-sm sm:text-base transition-all ${
                dataType === "temperature"
                  ? "bg-blue-600 text-white shadow-lg scale-105"
                  : "bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-300 dark:hover:bg-gray-600"
              }`}
            >
              <IconTemperature size={20} className="sm:w-6 sm:h-6" />
              <span className="hidden sm:inline">Temperature</span>
              <span className="sm:hidden">Temp</span>
            </button>
            <button
              onClick={() => setDataType("precipitation")}
              className={`flex items-center gap-1.5 sm:gap-2 px-3 sm:px-5 py-2 sm:py-3 rounded-lg font-semibold text-sm sm:text-base transition-all ${
                dataType === "precipitation"
                  ? "bg-blue-600 text-white shadow-lg scale-105"
                  : "bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-300 dark:hover:bg-gray-600"
              }`}
            >
              <IconDroplet size={20} className="sm:w-6 sm:h-6" />
              <span className="hidden sm:inline">Precipitation</span>
              <span className="sm:hidden">Rain</span>
            </button>
            <button
              onClick={() => setDataType("wind")}
              className={`flex items-center gap-1.5 sm:gap-2 px-3 sm:px-5 py-2 sm:py-3 rounded-lg font-semibold text-sm sm:text-base transition-all ${
                dataType === "wind"
                  ? "bg-blue-600 text-white shadow-lg scale-105"
                  : "bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-300 dark:hover:bg-gray-600"
              }`}
            >
              <IconWind size={20} className="sm:w-6 sm:h-6" />
              <span className="hidden sm:inline">Wind Speed</span>
              <span className="sm:hidden">Wind</span>
            </button>
          </div>

          {/* Chart */}
          <div ref={containerRef} className="bg-gradient-to-br from-gray-50 to-gray-100 dark:from-gray-800 dark:to-gray-900 rounded-xl p-3 sm:p-4 mb-4 sm:mb-6 shadow-inner">
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
                      style={{ fontSize: width < 500 ? '9px' : '12px' }}
                    >
                      {value.toFixed(width < 500 ? 0 : 1)}
                    </text>
                  </g>
                );
              })}

              {/* X-axis labels */}
              {displayData.map((h, i) => {
                // Show fewer labels on mobile
                const skipInterval = width < 500 ? 4 : 3;
                if (i % skipInterval !== 0) return null;
                const x = padding.left + (i / (values.length - 1)) * chartWidth;
                const time = new Date(h.time);
                const label = time.getHours().toString().padStart(2, "0") + ":00";
                return (
                  <text
                    key={i}
                    x={x}
                    y={height - padding.bottom + 20}
                    textAnchor="middle"
                    className="text-[10px] sm:text-xs fill-gray-600 dark:fill-gray-400"
                    style={{ fontSize: width < 500 ? '9px' : '12px' }}
                  >
                    {label}
                  </text>
                );
              })}

              {/* Gradient definition for line */}
              <defs>
                <linearGradient id="lineGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                  <stop offset="0%" stopColor="rgb(59, 130, 246)" stopOpacity="0.8" />
                  <stop offset="100%" stopColor="rgb(37, 99, 235)" stopOpacity="1" />
                </linearGradient>
                <linearGradient id="areaGradient" x1="0%" y1="0%" x2="0%" y2="100%">
                  <stop offset="0%" stopColor="rgb(59, 130, 246)" stopOpacity="0.3" />
                  <stop offset="100%" stopColor="rgb(59, 130, 246)" stopOpacity="0.05" />
                </linearGradient>
              </defs>

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
                strokeWidth={width < 500 ? "2.5" : "3"}
                strokeLinecap="round"
                strokeLinejoin="round"
              />

              {/* Data points */}
              {values.map((value, index) => {
                const x = padding.left + (index / (values.length - 1)) * chartWidth;
                const y = padding.top + chartHeight - ((value - minValue) / range) * chartHeight;
                const pointRadius = width < 500 ? 3 : 4;
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
            </svg>
          </div>

          {/* Page Navigation */}
          <div className="flex justify-center gap-2 sm:gap-3">
            <button
              onClick={() => setPage(0)}
              disabled={page === 0}
              className={`px-4 sm:px-6 py-2 sm:py-2.5 rounded-lg font-medium text-sm sm:text-base transition-all ${
                page === 0
                  ? "bg-blue-600 text-white shadow-lg"
                  : "bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-300 dark:hover:bg-gray-600 hover:shadow-md"
              } disabled:opacity-50 disabled:cursor-not-allowed`}
            >
              <span className="hidden sm:inline">First 24 Hours</span>
              <span className="sm:hidden">First 24h</span>
            </button>
            <button
              onClick={() => setPage(1)}
              disabled={page === 1 || hourlyData.length <= 24}
              className={`px-4 sm:px-6 py-2 sm:py-2.5 rounded-lg font-medium text-sm sm:text-base transition-all ${
                page === 1
                  ? "bg-blue-600 text-white shadow-lg"
                  : "bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-300 dark:hover:bg-gray-600 hover:shadow-md"
              } disabled:opacity-50 disabled:cursor-not-allowed`}
            >
              <span className="hidden sm:inline">Next 24 Hours</span>
              <span className="sm:hidden">Next 24h</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
