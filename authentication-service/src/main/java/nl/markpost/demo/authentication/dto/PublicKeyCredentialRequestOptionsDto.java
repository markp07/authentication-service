package nl.markpost.demo.authentication.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.PublicKeyCredentialRequestOptions;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PublicKeyCredentialRequestOptionsDto {

  private String challenge;
  private Long timeout;
  private String rpId;
  private List<PublicKeyCredentialDescriptor> allowCredentials;
  private String userVerification;
  private Map<String, Object> extensions;
  private List<String> hints;

  public static PublicKeyCredentialRequestOptionsDto from(
      PublicKeyCredentialRequestOptions options) {
    PublicKeyCredentialRequestOptionsDto dto = new PublicKeyCredentialRequestOptionsDto();
    dto.setChallenge(options.getChallenge().getBase64Url());
    dto.setTimeout(options.getTimeout().orElse(null));
    dto.setRpId(options.getRpId());

    // Handle allowCredentials Optional
    dto.setAllowCredentials(
        options.getAllowCredentials().isPresent() && !options.getAllowCredentials().get().isEmpty()
            ? options.getAllowCredentials().get()
            : null
    );

    // Handle userVerification Optional
    dto.setUserVerification(
        options.getUserVerification().isPresent()
            ? options.getUserVerification().get().getValue()
            : null
    );

    // Handle extensions - convert to simple map
    dto.setExtensions(null); // Simplify for now to avoid complex conversion

    // Handle hints
    dto.setHints(
        options.getHints() != null && !options.getHints().isEmpty()
            ? options.getHints()
            : null
    );

    return dto;
  }
}

