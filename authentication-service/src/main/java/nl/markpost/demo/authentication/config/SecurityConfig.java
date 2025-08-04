package nl.markpost.demo.authentication.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import nl.markpost.demo.authentication.filter.JwtAuthenticationFilter;
import nl.markpost.demo.authentication.filter.TraceparentFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Security configuration for the weather service.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final TraceparentFilter traceparentFilter;

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  /**
   * Creates a CORS filter bean for local development.
   *
   * @return CorsFilter configured for local development
   */
  @Bean
  @Profile("local")
  public CorsFilter corsFilter(
      @Value("${authentication.cors.allowed-origin-patterns:}") String[] allowedOriginPatterns) {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.setAllowedOriginPatterns(
        allowedOriginPatterns != null ? List.of(allowedOriginPatterns) : List.of());
    config.setAllowedHeaders(List.of("*"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }

  @Bean
  @Profile("!ut")
  public SecurityFilterChain filterChain(HttpSecurity http,
      @Value("${security.excluded-paths:}") String[] excludedPaths) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(AbstractHttpConfigurer::disable)
        .addFilterBefore(traceparentFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(authz -> authz
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers(excludedPaths).permitAll()
            .anyRequest().authenticated()
        );
    return http.build();
  }

  @Bean
  @Profile("ut")
  public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
