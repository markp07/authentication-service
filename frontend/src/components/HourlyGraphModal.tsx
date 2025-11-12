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

  // Chart dimensions
  const width = 800;
  const height = 300;
  const padding = { top: 20, right: 20, bottom: 40, left: 50 };
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
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4">
      <div className="bg-white dark:bg-gray-900 rounded-lg shadow-2xl max-w-5xl w-full max-h-[90vh] overflow-auto relative">
        <button
          className="absolute top-4 right-4 text-gray-500 hover:text-gray-900 dark:hover:text-white text-3xl font-bold focus:outline-none z-10"
          onClick={onClose}
          aria-label="Close modal"
        >
          ×
        </button>
        
        <div className="p-6">
          {/* Data Type Selector - Now serving as header */}
          <div className="flex gap-3 mb-6 flex-wrap justify-center">
            <button
              onClick={() => setDataType("temperature")}
              className={`flex items-center gap-2 px-6 py-3 rounded-lg font-semibold text-lg transition-colors ${
                dataType === "temperature"
                  ? "bg-blue-600 text-white shadow-lg"
                  : "bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-300 dark:hover:bg-gray-600"
              }`}
            >
              <IconTemperature size={24} />
              <span>Temperature</span>
            </button>
            <button
              onClick={() => setDataType("precipitation")}
              className={`flex items-center gap-2 px-6 py-3 rounded-lg font-semibold text-lg transition-colors ${
                dataType === "precipitation"
                  ? "bg-blue-600 text-white shadow-lg"
                  : "bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-300 dark:hover:bg-gray-600"
              }`}
            >
              <IconDroplet size={24} />
              <span>Precipitation</span>
            </button>
            <button
              onClick={() => setDataType("wind")}
              className={`flex items-center gap-2 px-6 py-3 rounded-lg font-semibold text-lg transition-colors ${
                dataType === "wind"
                  ? "bg-blue-600 text-white shadow-lg"
                  : "bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-300 dark:hover:bg-gray-600"
              }`}
            >
              <IconWind size={24} />
              <span>Wind Speed</span>
            </button>
          </div>

          {/* Chart */}
          <div className="bg-gray-50 dark:bg-gray-800 rounded-lg p-4 mb-6 overflow-x-auto">
            <svg
              width={width}
              height={height}
              className="mx-auto"
              style={{ minWidth: width }}
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
                      x={padding.left - 10}
                      y={y + 5}
                      textAnchor="end"
                      className="text-xs fill-gray-600 dark:fill-gray-400"
                    >
                      {value.toFixed(1)}
                    </text>
                  </g>
                );
              })}

              {/* X-axis labels */}
              {displayData.map((h, i) => {
                if (i % 3 !== 0) return null; // Show every 3rd label
                const x = padding.left + (i / (values.length - 1)) * chartWidth;
                const time = new Date(h.time);
                const label = time.getHours().toString().padStart(2, "0") + ":00";
                return (
                  <text
                    key={i}
                    x={x}
                    y={height - padding.bottom + 20}
                    textAnchor="middle"
                    className="text-xs fill-gray-600 dark:fill-gray-400"
                  >
                    {label}
                  </text>
                );
              })}

              {/* Line chart */}
              <path
                d={createPath()}
                fill="none"
                stroke="rgb(37, 99, 235)"
                strokeWidth="3"
                strokeLinecap="round"
                strokeLinejoin="round"
              />

              {/* Data points */}
              {values.map((value, index) => {
                const x = padding.left + (index / (values.length - 1)) * chartWidth;
                const y = padding.top + chartHeight - ((value - minValue) / range) * chartHeight;
                return (
                  <g key={index}>
                    <circle
                      cx={x}
                      cy={y}
                      r="4"
                      fill="rgb(37, 99, 235)"
                      className="hover:r-6 transition-all"
                    />
                    <title>{`${displayData[index].time}: ${value.toFixed(1)} ${getUnit()}`}</title>
                  </g>
                );
              })}
            </svg>
          </div>

          {/* Page Navigation */}
          <div className="flex justify-center gap-4">
            <button
              onClick={() => setPage(0)}
              disabled={page === 0}
              className={`px-6 py-2 rounded-lg font-medium transition-colors ${
                page === 0
                  ? "bg-blue-600 text-white"
                  : "bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-300 dark:hover:bg-gray-600"
              } disabled:opacity-50 disabled:cursor-not-allowed`}
            >
              First 24 Hours
            </button>
            <button
              onClick={() => setPage(1)}
              disabled={page === 1 || hourlyData.length <= 24}
              className={`px-6 py-2 rounded-lg font-medium transition-colors ${
                page === 1
                  ? "bg-blue-600 text-white"
                  : "bg-gray-200 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-300 dark:hover:bg-gray-600"
              } disabled:opacity-50 disabled:cursor-not-allowed`}
            >
              Next 24 Hours
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
