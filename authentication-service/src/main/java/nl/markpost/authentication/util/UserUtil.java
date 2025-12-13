package nl.markpost.authentication.util;

import com.yubico.webauthn.data.ByteArray;
import java.nio.charset.StandardCharsets;
import nl.markpost.authentication.model.User;
import nl.markpost.authentication.exception.InternalServerErrorException;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for retrieving User information from the security context.
 */
public class UserUtil {

  /**
   * Retrieves the User object from the security context.
   *
   * @return the User object
   * @throws InternalServerErrorException if the principal is not a User
   */
  public static User getUserFromSecurityContext() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (principal instanceof User user) {
      return user;
    } else {
      throw new InternalServerErrorException();
    }
  }

  /**
   * Converts the User's ID to a ByteArray.
   *
   * @param user the User object
   * @return the User's ID as ByteArray
   */
  public static ByteArray getIdAsByteArray(User user) {
    String uuid = user.getId().toString();
    return new ByteArray(uuid.getBytes(StandardCharsets.UTF_8));
  }

}
