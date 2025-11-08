package nl.markpost.demo.authentication.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CookieUtilTest {

  private CookieUtil cookieUtil;

  @BeforeEach
  void setUp() {
    cookieUtil = new CookieUtil();
  }

  @Test
  @DisplayName("Should build cookie with name, value and maxAge")
  void buildCookie_withNameValueMaxAge() {
    cookieUtil.setCookieSecure(true);
    Cookie cookie = CookieUtil.buildCookie("testCookie", "testValue", 3600);
    
    assertEquals("testCookie", cookie.getName());
    assertEquals("testValue", cookie.getValue());
    assertEquals(3600, cookie.getMaxAge());
    assertTrue(cookie.isHttpOnly());
    assertTrue(cookie.getSecure());
    assertEquals("/", cookie.getPath());
  }

  @Test
  @DisplayName("Should build cookie with name and value using default maxAge")
  void buildCookie_withNameValue() {
    cookieUtil.setCookieSecure(false);
    Cookie cookie = CookieUtil.buildCookie("testCookie", "testValue");
    
    assertEquals("testCookie", cookie.getName());
    assertEquals("testValue", cookie.getValue());
    assertEquals(0, cookie.getMaxAge());
    assertTrue(cookie.isHttpOnly());
    assertFalse(cookie.getSecure());
    assertEquals("/", cookie.getPath());
  }

  @Test
  @DisplayName("Should build cookie with name only")
  void buildCookie_withNameOnly() {
    cookieUtil.setCookieSecure(true);
    Cookie cookie = CookieUtil.buildCookie("testCookie");
    
    assertEquals("testCookie", cookie.getName());
    assertNull(cookie.getValue());
    assertEquals(0, cookie.getMaxAge());
    assertTrue(cookie.isHttpOnly());
    assertTrue(cookie.getSecure());
    assertEquals("/", cookie.getPath());
  }
}
