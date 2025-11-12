"use client";

import React from "react";
import HourlyGraphModal from "../../components/HourlyGraphModal";
import type { Hourly } from "../../types/Hourly";

// Generate mock hourly data for demonstration
const generateMockHourlyData = (): Hourly[] => {
  const data: Hourly[] = [];
  const now = new Date();
  
  for (let i = 0; i < 48; i++) {
    const time = new Date(now.getTime() + i * 60 * 60 * 1000);
    data.push({
      time: time.toISOString(),
      weatherCode: "1",
      temperature: 15 + Math.sin(i / 4) * 10 + Math.random() * 3,
      precipitationProbability: Math.floor(20 + Math.sin(i / 3) * 30 + Math.random() * 20),
      precipitation: Math.max(0, Math.sin(i / 5) * 2 + Math.random() * 1.5),
      windSpeed: Math.floor(10 + Math.sin(i / 6) * 15 + Math.random() * 5),
      windDirection: ["N", "NE", "E", "SE", "S", "SW", "W", "NW"][i % 8],
    });
  }
  
  return data;
};

export default function DemoPage() {
  const [showModal, setShowModal] = React.useState(false);
  const mockData = React.useMemo(() => generateMockHourlyData(), []);

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900 p-8">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold text-gray-900 dark:text-white mb-8">
          Hourly Graph Modal Demo
        </h1>
        
        <div className="bg-white dark:bg-gray-800 rounded-xl shadow-lg p-6">
          <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4">
            Weather Page Enhancement Demo
          </h2>
          <p className="text-gray-600 dark:text-gray-400 mb-6">
            This demo shows the new hourly graph modal with the following features:
          </p>
          <ul className="list-disc list-inside text-gray-600 dark:text-gray-400 mb-6 space-y-2">
            <li>Interactive line chart for visualizing hourly weather data</li>
            <li>Switch between Temperature, Precipitation, and Wind Speed data</li>
            <li>View first 24 hours or next 24 hours of forecast</li>
            <li>Hover over data points to see exact values</li>
          </ul>
          
          <button
            onClick={() => setShowModal(true)}
            className="px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium transition-colors"
          >
            Open Hourly Graph Modal
          </button>
        </div>

        <HourlyGraphModal
          open={showModal}
          onClose={() => setShowModal(false)}
          hourlyData={mockData}
        />
      </div>
    </div>
  );
}
