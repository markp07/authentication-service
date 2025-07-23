package nl.markpost.demo.weather.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Security configuration for the weather service.
 */
@Configuration
public class SecurityConfig {

  /**
   * List of allowed origin patterns for CORS, configurable via application.yml.
   * Defaults to an empty list if not set.
   */
  @Value("${weather.cors.allowed-origin-patterns:}")
  private final List<String> allowedOriginPatterns = List.of();

  /**
   * Creates a CORS filter bean for local development.
   *
   * @return CorsFilter configured for local development
   */
  @Bean
  @Profile("local")
  public CorsFilter corsFilter() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.setAllowedOriginPatterns(allowedOriginPatterns);
    config.setAllowedHeaders(List.of("*"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }

}
