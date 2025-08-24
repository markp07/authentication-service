package nl.markpost.demo.weather.model;

import lombok.Getter;

@Getter
public enum WeatherCode {

  CLEAR_SKY(0),
  MAINLY_CLEAR(1),
  PARTLY_CLOUDY(2),
  OVERCAST(3),
  FOG(45),
  DEPOSITING_RIME_FOG(48),
  DRIZZLE_LIGHT(51),
  DRIZZLE_MODERATE(53),
  DRIZZLE_DENSE(55),
  FREEZING_DRIZZLE_LIGHT(56),
  FREEZING_DRIZZLE_DENSE(57),
  RAIN_SLIGHT(61),
  RAIN_MODERATE(63),
  RAIN_HEAVY(65),
  FREEZING_RAIN_LIGHT(66),
  FREEZING_RAIN_HEAVY(67),
  SNOW_SLIGHT(71),
  SNOW_MODERATE(73),
  SNOW_HEAVY(75),
  SNOW_GRAINS(77),
  RAIN_SHOWERS_SLIGHT(80),
  RAIN_SHOWERS_MODERATE(81),
  RAIN_SHOWERS_VIOLENT(82),
  SNOW_SHOWERS_SLIGHT(85),
  SNOW_SHOWERS_HEAVY(86),
  THUNDERSTORM_SLIGHT_MODERATE(95),
  THUNDERSTORM_SLIGHT_HAIL(96),
  THUNDERSTORM_HEAVY_HAIL(99);

  private final int code;

  WeatherCode(int code) {
    this.code = code;
  }

  public static WeatherCode fromCode(int code) {
    for (WeatherCode wc : values()) {
      if (wc.code == code) {
        return wc;
      }
    }
    // fallback for grouped codes
    return switch (code) {
      case 1, 2, 3 -> MAINLY_CLEAR;
      case 45, 48 -> FOG;
      case 51, 53, 55 -> DRIZZLE_LIGHT;
      case 56, 57 -> FREEZING_DRIZZLE_LIGHT;
      case 61, 63, 65 -> RAIN_SLIGHT;
      case 66, 67 -> FREEZING_RAIN_LIGHT;
      case 71, 73, 75 -> SNOW_SLIGHT;
      case 80, 81, 82 -> RAIN_SHOWERS_SLIGHT;
      case 85, 86 -> SNOW_SHOWERS_SLIGHT;
      case 95 -> THUNDERSTORM_SLIGHT_MODERATE;
      case 96, 99 -> THUNDERSTORM_SLIGHT_HAIL;
      default -> CLEAR_SKY;
    };
  }
}

