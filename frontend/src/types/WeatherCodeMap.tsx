import { CloudRainHeavy, CloudDrizzle, CloudRain, Sun, Moon, Cloudy, CloudSun, CloudMoon, Clouds, CloudFog, CloudSleet, CloudSnow, CloudLightning , CloudHail, CloudLightningRain} from 'react-bootstrap-icons';

import { JSX } from "react";

/**
 * Helper function to determine if it's nighttime based on current time and sunrise/sunset
 * @param currentTime - Current time as ISO string
 * @param sunRise - Sunrise time as ISO string
 * @param sunSet - Sunset time as ISO string
 * @returns true if it's nighttime (before sunrise or after sunset)
 */
export function isNightTime(currentTime: string, sunRise: string, sunSet: string): boolean {
  const current = new Date(currentTime);
  const sunrise = new Date(sunRise);
  const sunset = new Date(sunSet);

  return current < sunrise || current > sunset;
}

export const weatherCodeMap: Record<string, { label: string; icon: (size?: number, isNightTime?: boolean) => JSX.Element }> = {
  CLEAR_SKY: { label: "Clear sky", icon: (size = 32, isNightTime = false) => isNightTime ? <Moon className="text-blue-300" size={size} /> : <Sun className="text-yellow-400" size={size} /> },
  MAINLY_CLEAR: { label: "Mainly clear", icon: (size = 32, isNightTime = false) => isNightTime ? <CloudMoon className="text-blue-300" size={size} /> : <CloudSun className="text-blue-400" size={size} /> },
  PARTLY_CLOUDY: { label: "Partly cloudy", icon: (size = 32, isNightTime = false) => <Cloudy className="text-blue-400" size={size} /> },
  OVERCAST: { label: "Overcast", icon: (size = 32, isNightTime = false) => <Clouds className="text-blue-400" size={size} /> },
  FOG: { label: "Fog", icon: (size = 32, isNightTime = false) => <CloudFog className="text-gray-400" size={size} /> },
  DEPOSITING_RIME_FOG: { label: "Depositing rime fog", icon: (size = 32, isNightTime = false) => <CloudFog className="text-gray-400" size={size} /> },
  DRIZZLE_LIGHT: { label: "Drizzle", icon: (size = 32, isNightTime = false) => <CloudDrizzle className="text-blue-400" size={size} /> },
  DRIZZLE_MODERATE: { label: "Drizzle", icon: (size = 32, isNightTime = false) => <CloudDrizzle className="text-blue-400" size={size} /> },
  DRIZZLE_DENSE: { label: "Drizzle", icon: (size = 32, isNightTime = false) => <CloudDrizzle className="text-blue-400" size={size} /> },
  FREEZING_DRIZZLE_LIGHT: { label: "Freezing Drizzle: Light", icon: (size = 32, isNightTime = false) => <CloudSleet className="text-blue-200" size={size} /> },
  FREEZING_DRIZZLE_DENSE: { label: "Freezing Drizzle: Dense", icon: (size = 32, isNightTime = false) => <CloudSleet className="text-blue-200" size={size} /> },
  RAIN_SLIGHT: { label: "Rain", icon: (size = 32, isNightTime = false) => <CloudRain className="text-blue-600" size={size} /> },
  RAIN_MODERATE: { label: "Rain", icon: (size = 32, isNightTime = false) => <CloudRain className="text-blue-600" size={size} /> },
  RAIN_HEAVY: { label: "Rain", icon: (size = 32, isNightTime = false) => <CloudRainHeavy className="text-blue-600" size={size} /> },
  FREEZING_RAIN_LIGHT: { label: "Freezing Rain: Light", icon: (size = 32, isNightTime = false) => <CloudSleet className="text-blue-200" size={size} /> },
  FREEZING_RAIN_HEAVY: { label: "Freezing Rain: Heavy", icon: (size = 32, isNightTime = false) => <CloudSleet className="text-blue-200" size={size} /> },
  SNOW_SLIGHT: { label: "Snow fall: Slight", icon: (size = 32, isNightTime = false) => <CloudSnow className="text-blue-200" size={size} /> },
  SNOW_MODERATE: { label: "Snow fall: Moderate", icon: (size = 32, isNightTime = false) => <CloudSnow className="text-blue-200" size={size} /> },
  SNOW_HEAVY: { label: "Snow fall: Heavy", icon: (size = 32, isNightTime = false) => <CloudSnow className="text-blue-200" size={size} /> },
  SNOW_GRAINS: { label: "Snow grains", icon: (size = 32, isNightTime = false) => <CloudSnow className="text-blue-200" size={size} /> },
  RAIN_SHOWERS_SLIGHT: { label: "Rain showers: Slight", icon: (size = 32, isNightTime = false) => <CloudDrizzle className="text-blue-400" size={size} /> },
  RAIN_SHOWERS_MODERATE: { label: "Rain showers: Moderate", icon: (size = 32, isNightTime = false) => <CloudRain className="text-blue-400" size={size} /> },
  RAIN_SHOWERS_VIOLENT: { label: "Rain showers: Violent", icon: (size = 32, isNightTime = false) => <CloudRainHeavy className="text-blue-400" size={size} /> },
  SNOW_SHOWERS_SLIGHT: { label: "Snow showers: Slight", icon: (size = 32, isNightTime = false) => <CloudSnow className="text-blue-400" size={size} /> },
  SNOW_SHOWERS_HEAVY: { label: "Snow showers: Heavy", icon: (size = 32, isNightTime = false) => <CloudSnow className="text-blue-400" size={size} /> },
  THUNDERSTORM_SLIGHT_MODERATE: { label: "Thunderstorm: Slight or moderate", icon: (size = 32, isNightTime = false) => <CloudLightning className="text-yellow-600" size={size} /> },
  THUNDERSTORM_SLIGHT_HAIL: { label: "Thunderstorm with slight hail", icon: (size = 32, isNightTime = false) => <CloudHail className="text-blue-400" size={size} /> },
  THUNDERSTORM_HEAVY_HAIL: { label: "Thunderstorm with heavy hail", icon: (size = 32, isNightTime = false) => <CloudLightningRain className="text-blue-400" size={size} /> },
};
