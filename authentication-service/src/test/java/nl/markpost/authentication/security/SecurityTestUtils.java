package nl.markpost.authentication.security;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SecurityTestUtils {

  public static final String TEST_PRIVATE_KEY_PATH = "classpath:keys/private_key.pem";
  public static final String TEST_PUBLIC_KEY_PATH = "classpath:keys/public_key.pem";

  private SecurityTestUtils() {
  }

  public static Path createTempPemFile(String prefix, String content) throws IOException {
    Path file = Files.createTempFile(prefix, ".pem");
    Files.writeString(file, content);
    file.toFile().deleteOnExit();
    return file;
  }

  public static String csrfCookie(String token) {
    return "XSRF-TOKEN=" + token;
  }
}

