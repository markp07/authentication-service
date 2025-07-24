package nl.markpost.demo.weather.config;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class SecurityConfig {

  @Value("${weather.cors.allowed-origin-patterns:}")
  private String[] allowedOriginPatterns;

  /**
   * Creates a CORS filter bean for local development.
   *
   * @return CorsFilter configured for local development
   */
  @Bean
  @Profile("local")
  public CorsFilter corsFilter() {
    log.info("CORS filter initialized with allowed origin patterns: {}", List.of(allowedOriginPatterns));
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.setAllowedOriginPatterns(allowedOriginPatterns != null ? List.of(allowedOriginPatterns) : List.of());
    config.setAllowedHeaders(List.of("*"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }

}
