package nl.markpost.demo.authentication.config;

import nl.markpost.demo.authentication.security.JwtAuthenticationFilter;
import nl.markpost.demo.authentication.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final SecurityProperties securityProperties;

  public SecurityConfig(SecurityProperties securityProperties) {
    this.securityProperties = securityProperties;
  }

  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService,
      UserDetailsService userDetailsService) {
    return new JwtAuthenticationFilter(jwtService, userDetailsService,
        securityProperties.getExcludedPaths());
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http,
      JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authz -> authz
            .requestMatchers(securityProperties.getExcludedPaths().toArray(new String[0]))
            .permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
