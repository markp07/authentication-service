package nl.markpost.demo.authentication.mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import nl.markpost.demo.authentication.api.v1.model.PasskeyInfoDto;
import nl.markpost.demo.authentication.model.PasskeyCredential;
import org.mapstruct.Mapper;

/**
 * Mapper for converting PasskeyCredential to PasskeyInfoDto.
 */
@Mapper(componentModel = "spring")
public interface PasskeyInfoDtoMapper {

  /**
   * Maps PasskeyCredential to PasskeyInfoDto.
   *
   * @param passkeyCredential the PasskeyCredential object
   * @return the mapped PasskeyInfoDto
   */
  PasskeyInfoDto from(PasskeyCredential passkeyCredential);

  /**
   * Maps LocalDateTime to OffsetDateTime in UTC.
   *
   * @param localDateTime the LocalDateTime object
   * @return the mapped OffsetDateTime object
   */
  default OffsetDateTime map(LocalDateTime localDateTime) {
    return localDateTime != null ? localDateTime.atOffset(ZoneOffset.UTC) : null;
  }

}
