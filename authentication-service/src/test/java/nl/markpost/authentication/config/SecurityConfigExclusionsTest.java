package nl.markpost.authentication.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import tools.jackson.databind.ObjectMapper;
import nl.markpost.authentication.exception.CustomExceptionHandler;
import nl.markpost.authentication.filter.JwtAuthenticationFilter;
import nl.markpost.authentication.security.JwtKeyProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;

class SecurityConfigExclusionsTest {

  private JwtAuthenticationFilter createFilterWithPatterns(String... patterns) {
    JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
        Mockito.mock(UserDetailsService.class),
        Mockito.mock(CustomExceptionHandler.class),
        new ObjectMapper(),
        Mockito.mock(JwtKeyProvider.class));
    ReflectionTestUtils.setField(filter, "excludedPaths", patterns);
    return filter;
  }

  @Test
  @DisplayName("Should match wildcard exclusions with Ant-style patterns")
  void shouldMatchAntStylePatterns() {
    JwtAuthenticationFilter filter = createFilterWithPatterns("/v1/public/**", "/v1/csrf");

    Boolean matched = ReflectionTestUtils.invokeMethod(filter, "isExcludedPath", "/v1/public/health");

    assertTrue(Boolean.TRUE.equals(matched));
  }

  @Test
  @DisplayName("Should deny non-matching paths by default")
  void shouldDenyWhenNoPatternMatches() {
    JwtAuthenticationFilter filter = createFilterWithPatterns("/v1/public/**");

    Boolean matched = ReflectionTestUtils.invokeMethod(filter, "isExcludedPath", "/v1/admin/users");

    assertFalse(Boolean.TRUE.equals(matched));
  }
}

