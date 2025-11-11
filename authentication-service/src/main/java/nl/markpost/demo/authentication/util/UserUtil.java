package nl.markpost.demo.authentication.util;

import com.yubico.webauthn.data.ByteArray;
import java.nio.charset.StandardCharsets;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.common.exception.InternalServerErrorException;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserUtil {

  public static User getUserFromSecurityContext() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (principal instanceof User user) {
      return user;
    } else {
      throw new InternalServerErrorException();
    }
  }

  public static ByteArray getIdAsByteArray(User user) {
    String uuid = user.getId().toString();
    return new ByteArray(uuid.getBytes(StandardCharsets.UTF_8));
  }

}
