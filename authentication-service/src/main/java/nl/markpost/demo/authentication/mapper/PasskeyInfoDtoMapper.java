package nl.markpost.demo.authentication.mapper;

import nl.markpost.demo.authentication.api.v1.model.PasskeyInfoDto;
import nl.markpost.demo.authentication.model.PasskeyCredential;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PasskeyInfoDtoMapper {

  PasskeyInfoDto from(PasskeyCredential passkeyCredential);

}
