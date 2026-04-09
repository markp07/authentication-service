package nl.markpost.authentication.util;

import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

  private static boolean cookieSecure;

  @Value("${cookie.secure:true}")
  public void setCookieSecure(boolean secure) {
    cookieSecure = secure;
  }

  private static String cookieDomain;

  @Value("${cookie.domain:yourdomain.tld}")
  public void setDomain(String domain) {
    cookieDomain = domain;
  }

  public static Cookie buildCookie(String name, String value, int maxAge) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);
    cookie.setSecure(cookieSecure);
    cookie.setDomain(cookieDomain);
    cookie.setPath("/");
    cookie.setMaxAge(maxAge);
    return cookie;
  }

  public static Cookie buildNonHttpOnlyCookie(String name, String value, int maxAge) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(false);
    cookie.setSecure(cookieSecure);
    cookie.setDomain(cookieDomain);
    cookie.setPath("/");
    cookie.setMaxAge(maxAge);
    return cookie;
  }

  public static Cookie buildCookie(String name, String value) {
    return buildCookie(name, value, 0);
  }

  public static Cookie buildCookie(String name) {
    return buildCookie(name, null);
  }
}
