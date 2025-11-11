package nl.markpost.demo.authentication.mapper;

import com.yubico.webauthn.RegistrationResult;
import nl.markpost.demo.authentication.model.PasskeyCredential;
import nl.markpost.demo.authentication.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface PasskeyCredentialMapper {

  @Mapping(target = "user", source = "user")
  @Mapping(target = "credentialId", expression = "java(registrationResult.getKeyId().getId().getBase64Url())")
  @Mapping(target = "publicKey", expression = "java(registrationResult.getPublicKeyCose().getBase64Url())")
  @Mapping(target = "name", source = "name")
  @Mapping(target = "createdAt", expression = "java(buildCreatedAt())")
  PasskeyCredential from(RegistrationResult registrationResult, User user, String name);

  default LocalDateTime buildCreatedAt() {
    return LocalDateTime.now();
  }
}

