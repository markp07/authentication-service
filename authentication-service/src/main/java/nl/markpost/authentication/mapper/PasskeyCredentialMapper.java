package nl.markpost.authentication.mapper;

import com.yubico.webauthn.RegistrationResult;
import java.time.LocalDateTime;
import nl.markpost.authentication.model.PasskeyCredential;
import nl.markpost.authentication.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for converting RegistrationResult and User to PasskeyCredential.
 */
@Mapper(componentModel = "spring")
public interface PasskeyCredentialMapper {

  /**
   * Maps RegistrationResult and User to PasskeyCredential.
   *
   * @param registrationResult the RegistrationResult object
   * @param user               the User object
   * @param name               the name for the PasskeyCredential
   * @return the mapped PasskeyCredential
   */
  @Mapping(target = "user", source = "user")
  @Mapping(target = "credentialId", expression = "java(registrationResult.getKeyId().getId().getBase64Url())")
  @Mapping(target = "publicKey", expression = "java(registrationResult.getPublicKeyCose().getBase64Url())")
  @Mapping(target = "name", source = "name")
  @Mapping(target = "createdAt", expression = "java(buildCreatedAt())")
  PasskeyCredential from(RegistrationResult registrationResult, User user, String name);

  /**
   * Builds the createdAt timestamp.
   *
   * @return the current LocalDateTime
   */

  default LocalDateTime buildCreatedAt() {
    return LocalDateTime.now();
  }
}

