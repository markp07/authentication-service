package nl.markpost.demo.authentication.mapper;

import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ResidentKeyRequirement;
import com.yubico.webauthn.data.UserIdentity;
import com.yubico.webauthn.data.UserVerificationRequirement;
import nl.markpost.demo.authentication.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for converting User and user ID to StartRegistrationOptions.
 */
@Mapper(componentModel = "spring")
public interface StartRegistrationOptionsMapper {

  /**
   * Maps user ID bytes and User object to StartRegistrationOptions.
   *
   * @param userIdBytes the user ID as ByteArray
   * @param user the User object
   * @return the mapped StartRegistrationOptions
   */
  @Mapping(target = "user", expression = "java(mapUserIdentity(userIdBytes, user))")
  @Mapping(target = "authenticatorSelection", expression = "java(buildAuthenticatorSelection())")
  StartRegistrationOptions from(ByteArray userIdBytes, User user);

  /**
   * Maps user ID bytes and User object to UserIdentity.
   *
   * @param userIdBytes the user ID as ByteArray
   * @param user the User object
   * @return the mapped UserIdentity
   */
  @Mapping(target = "id", source = "userIdBytes")
  @Mapping(target = "name", source = "user.email")
  @Mapping(target = "displayName", source = "user.username")
  UserIdentity mapUserIdentity(ByteArray userIdBytes, User user);

  /**
   * Builds default AuthenticatorSelectionCriteria.
   *
   * @return the built AuthenticatorSelectionCriteria
   */
  default AuthenticatorSelectionCriteria buildAuthenticatorSelection() {
    return AuthenticatorSelectionCriteria.builder()
        .residentKey(ResidentKeyRequirement.REQUIRED)
        .userVerification(UserVerificationRequirement.REQUIRED)
        .build();
  }

}
