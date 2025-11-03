export interface Daily {
  time: string;
  weatherCode: string;
  temperatureMin: number;
  temperatureMax: number;
  precipitation: number;
  precipitationProbabilityMax: number;
  windSpeed: number;
  windDirection: string;
  sunRise: string;
  sunSet: string;
}