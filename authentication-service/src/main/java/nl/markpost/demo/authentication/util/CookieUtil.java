package nl.markpost.demo.authentication.util;

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

  public static Cookie buildCookie(String name, String value, int maxAge) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);
    cookie.setSecure(cookieSecure);
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
