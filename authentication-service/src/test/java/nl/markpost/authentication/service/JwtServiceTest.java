package nl.markpost.authentication.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import nl.markpost.authentication.model.User;
import nl.markpost.authentication.security.JwtKeyProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

  @Mock
  private JwtKeyProvider keyProvider;

  @InjectMocks
  private JwtService jwtService;

  private User user;
  private java.security.PrivateKey privateKey;
  private java.security.PublicKey publicKey;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setId(java.util.UUID.randomUUID());
    user.setEmail("user@example.com");
    // Use a generated key pair for testing
    java.security.KeyPair keyPair = io.jsonwebtoken.security.Keys.keyPairFor(
        SignatureAlgorithm.RS256);
    privateKey = keyPair.getPrivate();
    publicKey = keyPair.getPublic();
  }

  @Test
  @DisplayName("Should generate access token containing correct email and expiration")
  void generateAccessToken_success() {
    when(keyProvider.getPrivateKey()).thenReturn(privateKey);
    String token = jwtService.generateAccessToken(user);
    assertNotNull(token);
    String email = Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token)
        .getPayload().getSubject();
    assertEquals(user.getEmail(), email);
  }

  @Test
  @DisplayName("Should generate refresh token containing correct email and expiration")
  void generateRefreshToken_success() {
    when(keyProvider.getPrivateKey()).thenReturn(privateKey);
    String token = jwtService.generateRefreshToken(user);
    assertNotNull(token);
    String email = Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token)
        .getPayload().getSubject();
    assertEquals(user.getEmail(), email);
  }

  @Test
  @DisplayName("Should extract email from token")
  void getEmailFromToken_success() {
    when(keyProvider.getPrivateKey()).thenReturn(privateKey);
    when(keyProvider.getPublicKey()).thenReturn(publicKey);
    String token = jwtService.generateAccessToken(user);
    String email = jwtService.getEmailFromToken(token);
    assertEquals(user.getEmail(), email);
  }
}
