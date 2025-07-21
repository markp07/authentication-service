package nl.markpost.demo.authentication.util;

import jakarta.servlet.http.Cookie;

public class CookieUtil {

  public static Cookie buildCookie(String name, String value, int maxAge) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setMaxAge(maxAge);
    cookie.setDomain("localhost");
    return cookie;
  }

  public static Cookie buildCookie(String name, String value) {
    return buildCookie(name, value, 0);
  }

  public static Cookie buildCookie(String name) {
    return buildCookie(name, null);
  }
}
