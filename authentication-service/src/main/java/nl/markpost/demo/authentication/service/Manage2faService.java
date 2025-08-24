package nl.markpost.demo.authentication.service;

import static nl.markpost.demo.authentication.constant.Constants.ACCESS_TOKEN;
import static nl.markpost.demo.authentication.constant.Constants.DAYS_7;
import static nl.markpost.demo.authentication.constant.Constants.MINUTES_15;
import static nl.markpost.demo.authentication.constant.Constants.REFRESH_TOKEN;
import static nl.markpost.demo.authentication.constant.Constants.TEMPORARY_TOKEN;
import static nl.markpost.demo.authentication.util.RequestUtil.getCurrentRequest;
import static nl.markpost.demo.authentication.util.RequestUtil.getEmailFromClaims;

import com.bastiaanjansen.otp.SecretGenerator;
import com.bastiaanjansen.otp.TOTPGenerator;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.markpost.demo.authentication.api.v1.model.BackupCodeResponse;
import nl.markpost.demo.authentication.api.v1.model.PasswordRequest;
import nl.markpost.demo.authentication.api.v1.model.TOTPCode;
import nl.markpost.demo.authentication.api.v1.model.TOTPSetupResponse;
import nl.markpost.demo.authentication.api.v1.model.TOTPVerifyRequest;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.repository.UserRepository;
import nl.markpost.demo.authentication.util.CookieUtil;
import nl.markpost.demo.authentication.util.RequestUtil;
import nl.markpost.demo.common.exception.BadRequestException;
import nl.markpost.demo.common.exception.ForbiddenException;
import nl.markpost.demo.common.exception.NotFoundException;
import nl.markpost.demo.common.exception.UnauthorizedException;
import org.apache.commons.codec.binary.Base32;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service for managing two-factor authentication (2FA) setup and verification. Provides methods to
 * set up 2FA, enable it, and verify 2FA codes.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class Manage2faService {

  private static final String ISSUER = "Demo Authentication Service";
  private static final int SECRET_LENGTH = 20;

  private final UserRepository userRepository;

  private final JwtService jwtService;

  private final PasswordEncoder passwordEncoder;

  /**
   * Sets up two-factor authentication (2FA) for the user. Generates a TOTP secret, creates an
   * otpauth URI, and returns a response with the QR code.
   *
   * @return TOTPSetupResponse containing the otpauth URI, secret, and QR code image in Base64
   * format.
   * <p>
   * TODO: Need to store what time setup was triggered. Only allow enabling 2FA within a certain time frame (e.g., 5 minutes).
   */
  public TOTPSetupResponse setup2fa() {
    HttpServletRequest request = getCurrentRequest();
    String email = getEmailFromClaims(request);
    User user = userRepository.findByEmail(email);
    if (user == null) {
      throw new BadRequestException("User not found"); //TODO: Use a more specific message
    }
    if (user.is2faEnabled()) {
      throw new BadRequestException(
          "2FA is already enabled for this user."); //TODO: Use a more specific message
    }

    byte[] secret = SecretGenerator.generate(SECRET_LENGTH);
    String base32Secret = new Base32().encodeToString(secret).replace("=", "");
    user.setTotpSecret(base32Secret);
    user.set2faEnabled(false);
    user.setTotpSetupCreatedAt(LocalDateTime.now());
    userRepository.save(user);
    String otpauth = String.format(
        "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
        ISSUER,
        email,
        base32Secret,
        ISSUER
    );

    return TOTPSetupResponse.builder()
        .otpUri(otpauth)
        .secret(base32Secret)
        .qrCodeImage(generateQrCodeBase64(otpauth))
        .build();
  }

  /**
   * Enables two-factor authentication (2FA) for the user by verifying the provided TOTP code.
   *
   * @param code the TOTP code to verify
   *             <p>
   *                                     TODO: need check what time enabling 2FA was triggered. Only allow enabling 2FA within a certain time frame (e.g., 5 minutes).
   */
  public void enable2fa(TOTPCode code) {
    HttpServletRequest request = getCurrentRequest();
    String email = getEmailFromClaims(request);
    User user = userRepository.findByEmail(email);
    if (user == null || user.getTotpSecret() == null) {
      throw new BadRequestException(
          "User not found or 2FA not set up"); //TODO: Use a more specific message
    }
    if (user.getTotpSetupCreatedAt() == null
        || Duration.between(user.getTotpSetupCreatedAt(), LocalDateTime.now()).toMinutes() > 5) {
      throw new BadRequestException("2FA setup expired. Please set up 2FA again.");
    }
    if (verifyTotpCode(user.getTotpSecret(), code.getCode())) {
      user.set2faEnabled(true);
      user.setTotpSetupCreatedAt(null);
      userRepository.save(user);
    } else {
      throw new ForbiddenException();
    }
  }

  /**
   * Verifies the provided TOTP code for two-factor authentication (2FA).
   *
   * @param totpVerifyRequest the request containing the email and TOTP code to verify
   */
  public void verify2fa(TOTPVerifyRequest totpVerifyRequest) {
    HttpServletRequest request = RequestUtil.getCurrentRequest();
    String temporaryToken = null;
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if (TEMPORARY_TOKEN.equals(cookie.getName())) {
          temporaryToken = cookie.getValue();
          break;
        }
      }
    }
    if (temporaryToken == null) {
      throw new UnauthorizedException();
    }

    String email = jwtService.getEmailFromToken(temporaryToken);
    User user = userRepository.findByEmail(email);
    if (user == null || user.getTotpSecret() == null || !user.is2faEnabled()) {
      log.info("User not found or 2FA not set up for email: {}", email);
      throw new ForbiddenException();
    }
    if (verifyTotpCode(user.getTotpSecret(), totpVerifyRequest.getCode())) {
      HttpServletResponse response = RequestUtil.getCurrentResponse();

      String accessToken = jwtService.generateAccessToken(user);
      response.addCookie(CookieUtil.buildCookie(ACCESS_TOKEN, accessToken, MINUTES_15));

      String refreshToken = jwtService.generateRefreshToken(user);
      response.addCookie(CookieUtil.buildCookie(REFRESH_TOKEN, refreshToken, DAYS_7));
    } else {
      log.warn("Invalid TOTP code provided for email: {}", email);
      throw new ForbiddenException();
    }
  }

  /**
   * Generates a Base64 encoded QR code image from the provided otpauth URI.
   *
   * @param otpAuthUri the otpauth URI to encode in the QR code
   * @return Base64 encoded string of the QR code image
   */
  @SneakyThrows
  private String generateQrCodeBase64(String otpAuthUri) {
    QRCodeWriter qrCodeWriter = new QRCodeWriter();
    BitMatrix bitMatrix = qrCodeWriter.encode(otpAuthUri, BarcodeFormat.QR_CODE, 300, 300);

    BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(qrImage, "PNG", baos);

    return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
  }

  /**
   * Verifies the provided TOTP code against the user's secret.
   *
   * @param base32Secret the user's TOTP secret in Base32 format
   * @param code         the TOTP code to verify
   * @return true if the code is valid, false otherwise
   */
  protected boolean verifyTotpCode(String base32Secret, String code) {
    try {
      byte[] secret = base32Secret.getBytes(StandardCharsets.UTF_8);
      TOTPGenerator totp = new TOTPGenerator.Builder(secret).build();
      return totp.verify(code);
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Disables two-factor authentication (2FA) for the user after verifying the password.
   *
   * @param passwordRequest the request containing the user's password
   */
  public void disable2fa(PasswordRequest passwordRequest) {
    HttpServletRequest request = getCurrentRequest();
    String email = getEmailFromClaims(request);
    User user = userRepository.findByEmail(email);
    if (user == null) {
      throw new BadRequestException("User not found");
    }
    if (!user.is2faEnabled()) {
      throw new BadRequestException("2FA is not enabled for this user.");
    }
    if (!passwordEncoder.matches(passwordRequest.getPassword(), user.getPassword())) {
      throw new UnauthorizedException();
    }
    user.set2faEnabled(false);
    user.setTotpSecret(null);
    userRepository.save(user);
  }

  /**
   * Generates and stores a new 2FA backup code for the current user.
   *
   * @return the generated backup code
   */
  public BackupCodeResponse generateBackupCode() {
    HttpServletRequest request = getCurrentRequest();
    String email = getEmailFromClaims(request);
    User user = userRepository.findByEmail(email);
    if (user == null) {
      throw new NotFoundException("User not found");
    }
    if (!user.is2faEnabled()) {
      throw new BadRequestException("2FA is not enabled for this user.");
    }
    String backupCode = generateRandomBackupCode();
    String hashedBackupCode = passwordEncoder.encode(backupCode);
    user.setBackupCode(hashedBackupCode);
    userRepository.save(user);

    return BackupCodeResponse.builder()
        .backupCode(backupCode)
        .build();
  }

  private String generateRandomBackupCode() {
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    int length = 24;
    StringBuilder code = new StringBuilder();
    for (int i = 0; i < length; i++) {
      int idx = (int) (Math.random() * chars.length());
      code.append(chars.charAt(idx));
    }
    return code.toString();
  }

  /**
   * Resets (disables) 2FA for the current user using the backup code.
   *
   * @param backupCode the backup code provided by the user
   * @return true if successful, false otherwise
   */
  public boolean reset2faWithBackupCode(String backupCode) {
    HttpServletRequest request = getCurrentRequest();
    String email = getEmailFromClaims(request);
    User user = userRepository.findByEmail(email);
    if (user == null || user.getBackupCode() == null) {
      return false;
    }
    if (!passwordEncoder.matches(backupCode, user.getBackupCode())) {
      return false;
    }
    user.set2faEnabled(false);
    user.setTotpSecret(null);
    user.setBackupCode(null);
    userRepository.save(user);
    return true;
  }
}
