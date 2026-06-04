package nl.markpost.authentication.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.authentication.filter.CorsErrorHeaderFilter;
import nl.markpost.authentication.filter.JwtAuthenticationFilter;
import nl.markpost.authentication.filter.TraceparentFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
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
@Slf4j
public class SecurityConfig {

  private final TraceparentFilter traceparentFilter;

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  private final CorsErrorHeaderFilter corsErrorHeaderFilter;

  /**
   * Creates a CORS filter bean for all profiles.
   *
   * @return CorsFilter configured with allowed origin patterns
   */
  @Bean
  public CorsFilter corsFilter(
      @Value("${authentication.cors.allowed-origin-patterns:}") String[] allowedOriginPatterns) {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.setAllowedOriginPatterns(
        allowedOriginPatterns != null ? List.of(allowedOriginPatterns) : List.of());
    config.setAllowedHeaders(List.of("*"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setExposedHeaders(List.of("*"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }

  @Bean
  public CookieCsrfTokenRepository csrfTokenRepository(
      @Value("${cookie.domain:}") String cookieDomain,
      @Value("${cookie.secure:true}") boolean cookieSecure) {
    CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
    repository.setCookieName("XSRF-TOKEN");
    repository.setHeaderName("X-XSRF-TOKEN");
    repository.setCookiePath("/");
    repository.setCookieCustomizer(builder -> builder
        .sameSite("Lax")
        .path("/")
        .httpOnly(false)
        .secure(cookieSecure)
        .domain(cookieDomain.isBlank() ? null : cookieDomain));
    return repository;
  }

  @Bean
  @Profile("!ut")
  public SecurityFilterChain filterChain(HttpSecurity http,
      @Value("${security.excluded-paths:}") String[] excludedPaths,
      CookieCsrfTokenRepository csrfTokenRepository) {
    http
        .csrf(csrf -> csrf.csrfTokenRepository(csrfTokenRepository))
        .cors(Customizer.withDefaults()) // Enable CORS with default configuration (uses CorsFilter bean)
        .addFilterBefore(traceparentFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(corsErrorHeaderFilter, JwtAuthenticationFilter.class)
        .authorizeHttpRequests(authz -> authz
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers(excludedPaths).permitAll()
            .requestMatchers("/v1/admin/**").hasAuthority("ADMIN")
            .anyRequest().authenticated()
        );
    return http.build();
  }

  @Bean
  @Profile("ut")
  public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) {
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
