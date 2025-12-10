// Mapping of weather codes to translation keys
export const weatherCodeToTranslationKey: Record<string, string> = {
  CLEAR_SKY: 'clearSky',
  MAINLY_CLEAR: 'mainlyClear',
  PARTLY_CLOUDY: 'partlyCloudy',
  OVERCAST: 'overcast',
  FOG: 'fog',
  DEPOSITING_RIME_FOG: 'depositingRimeFog',
  DRIZZLE_LIGHT: 'drizzle',
  DRIZZLE_MODERATE: 'drizzle',
  DRIZZLE_DENSE: 'drizzle',
  FREEZING_DRIZZLE_LIGHT: 'freezingDrizzleLight',
  FREEZING_DRIZZLE_DENSE: 'freezingDrizzleDense',
  RAIN_SLIGHT: 'rain',
  RAIN_MODERATE: 'rain',
  RAIN_HEAVY: 'rain',
  FREEZING_RAIN_LIGHT: 'freezingRainLight',
  FREEZING_RAIN_HEAVY: 'freezingRainHeavy',
  SNOW_SLIGHT: 'snowSlight',
  SNOW_MODERATE: 'snowModerate',
  SNOW_HEAVY: 'snowHeavy',
  SNOW_GRAINS: 'snowGrains',
  RAIN_SHOWERS_SLIGHT: 'rainShowersSlight',
  RAIN_SHOWERS_MODERATE: 'rainShowersModerate',
  RAIN_SHOWERS_VIOLENT: 'rainShowersViolent',
  SNOW_SHOWERS_SLIGHT: 'snowShowersSlight',
  SNOW_SHOWERS_HEAVY: 'snowShowersHeavy',
  THUNDERSTORM_SLIGHT_MODERATE: 'thunderstormSlightModerate',
  THUNDERSTORM_SLIGHT_HAIL: 'thunderstormSlightHail',
  THUNDERSTORM_HEAVY_HAIL: 'thunderstormHeavyHail',
};

// Mapping of day numbers to translation keys (0 = Sunday, 1 = Monday, etc.)
export const dayNumberToTranslationKey = ['sun', 'mon', 'tue', 'wed', 'thu', 'fri', 'sat'];
