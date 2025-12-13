package nl.markpost.authentication.mapper;

import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.data.UserVerificationRequirement;
import lombok.AllArgsConstructor;
import nl.markpost.authentication.model.User;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting User and user ID to StartRegistrationOptions.
 */
@Component
@AllArgsConstructor
public class StartRegistrationOptionsMapper {

  /**
   * Maps user ID bytes and User object to StartRegistrationOptions.
   *
   * @param userIdBytes the user ID as ByteArray
   * @param user        the User object
   * @return the mapped StartRegistrationOptions
   */
  public StartRegistrationOptions from(ByteArray userIdBytes, User user) {
    return StartRegistrationOptions.builder()
        .user(mapUserIdentity(userIdBytes, user))
        .authenticatorSelection(buildAuthenticatorSelection())
        .build();
  }

  /**
   * Maps user ID bytes and User object to UserIdentity.
   *
   * @param userIdBytes the user ID as ByteArray
   * @param user        the User object
   * @return the mapped UserIdentity
   */
  private UserIdentity mapUserIdentity(ByteArray userIdBytes, User user) {
    return UserIdentity.builder()
        .name(user.getEmail())
        .displayName(user.getUsername())
        .id(userIdBytes)
        .build();
  }

  /**
   * Builds default AuthenticatorSelectionCriteria.
   *
   * @return the built AuthenticatorSelectionCriteria
   */
  private AuthenticatorSelectionCriteria buildAuthenticatorSelection() {
    return AuthenticatorSelectionCriteria.builder()
        .residentKey(ResidentKeyRequirement.REQUIRED)
        .userVerification(UserVerificationRequirement.REQUIRED)
        .build();
  }

}
