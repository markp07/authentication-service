package nl.markpost.demo.authentication.service;

import static nl.markpost.demo.authentication.constant.Constants.ACCESS_TOKEN;
import static nl.markpost.demo.authentication.constant.Constants.DAYS_7;
import static nl.markpost.demo.authentication.constant.Constants.MINUTES_15;
import static nl.markpost.demo.authentication.constant.Constants.REFRESH_TOKEN;
import static nl.markpost.demo.authentication.util.MessageResponseUtil.createMessageResponse;

import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.FinishAssertionOptions;
import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartAssertionOptions;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.UserIdentity;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import nl.markpost.demo.authentication.api.v1.model.Message;
import nl.markpost.demo.authentication.constant.Messages;
import nl.markpost.demo.authentication.model.PasskeyCredential;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.repository.PasskeyCredentialRepository;
import nl.markpost.demo.authentication.repository.UserRepository;
import nl.markpost.demo.authentication.util.CookieUtil;
import nl.markpost.demo.authentication.util.RequestUtil;
import nl.markpost.demo.common.exception.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasskeyService {

  private final PasskeyCredentialRepository passkeyCredentialRepository;
  private final UserRepository userRepository;
  private final RelyingParty relyingParty;
  private final JwtService jwtService;

  public List<PasskeyCredential> listPasskeys(String email) {
    User user = userRepository.findByEmail(email);
    return user != null ? passkeyCredentialRepository.findByUser(user) : List.of();
  }

  public void deletePasskey(String email, String credentialId) {
    User user = userRepository.findByEmail(email);
    if (user == null) {
      return;
    }
    PasskeyCredential cred = passkeyCredentialRepository.findByCredentialId(credentialId);
    if (cred != null && cred.getUser().equals(user)) {
      passkeyCredentialRepository.delete(cred);
    }
  }

  public PublicKeyCredentialCreationOptions startRegistration() {
    User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    String email = user.getEmail();
    String encodedEmail = Base64.getEncoder()
        .encodeToString(email.getBytes(StandardCharsets.UTF_8));
    return relyingParty.startRegistration(
        StartRegistrationOptions.builder()
            .user(UserIdentity.builder()
                .name(email)
                .displayName(user.getUsername())
                .id(ByteArray.fromBase64(encodedEmail))
                .build())
            .build()
    );
  }

  @SneakyThrows
  public void finishRegistration(PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> credential,
      String name) {
    User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    RegistrationResult result = relyingParty.finishRegistration(
        FinishRegistrationOptions.builder()
            .request(startRegistration())
            .response(credential)
            .build()
    );
    PasskeyCredential passkey = PasskeyCredential.builder()
        .user(user)
        .credentialId(result.getKeyId().getId().getBase64Url())
        .publicKey(result.getPublicKeyCose().getBase64Url())
        .name(name)
        .createdAt(LocalDateTime.now())
        .build();
    passkeyCredentialRepository.save(passkey);
  }

  public AssertionRequest startAuthentication(String email) {
    User user = userRepository.findByEmail(email);
    if (user == null) {
      throw new IllegalArgumentException("User not found");
    }
    return relyingParty.startAssertion(
        StartAssertionOptions.builder()
            .username(email)
            .build()
    );
  }

  @SneakyThrows
  public ResponseEntity<Message> finishAuthentication(String email,
      PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> credential) {
    AssertionResult result = relyingParty.finishAssertion(
        FinishAssertionOptions.builder()
            .request(startAuthentication(email))
            .response(credential)
            .build()
    );
    if (result.isSuccess()) {
      User user = userRepository.findByEmail(email);
      if (user == null) {
        throw new UnauthorizedException();
      }

      HttpServletResponse response = RequestUtil.getCurrentResponse();
      String accessToken = jwtService.generateAccessToken(user);
      response.addCookie(CookieUtil.buildCookie(ACCESS_TOKEN, accessToken, MINUTES_15));

      String refreshToken = jwtService.generateRefreshToken(user);
      response.addCookie(CookieUtil.buildCookie(REFRESH_TOKEN, refreshToken, DAYS_7));

      return ResponseEntity.status(HttpStatus.OK)
          .body(createMessageResponse(Messages.LOGIN_SUCCESS));
    } else {
      throw new UnauthorizedException();
    }
  }
}
