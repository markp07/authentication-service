package nl.markpost.demo.authentication.mapper;

import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import nl.markpost.demo.authentication.api.v1.model.PublicKeyCredentialCreationOptionsDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PublicKeyCredentialCreationOptionsDtoMapper {

  PublicKeyCredentialCreationOptionsDto from(PublicKeyCredentialCreationOptions options);

}
