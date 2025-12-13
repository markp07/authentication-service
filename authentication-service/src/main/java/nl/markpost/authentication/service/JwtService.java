package nl.markpost.authentication.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import nl.markpost.authentication.constant.Constants;
import nl.markpost.authentication.model.User;
import nl.markpost.authentication.security.JwtKeyProvider;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

  private final JwtKeyProvider keyProvider;

  public String generateAccessToken(User user) {
    long minutes15 = Constants.MINUTES_15 * 1000;
    long expirationTime = System.currentTimeMillis() + minutes15;
    return buildToken(user, expirationTime);
  }

  public String generateRefreshToken(User user) {
    long days7 = Constants.DAYS_7 * 1000;
    long expirationTime = System.currentTimeMillis() + days7;
    return buildToken(user, expirationTime);
  }

  private String buildToken(User user, long expirationTime) {
    return Jwts.builder()
        .subject(user.getEmail())
        .claim("userId", user.getId().toString())
        .issuedAt(new Date())
        .expiration(new Date(expirationTime))
        .signWith(keyProvider.getPrivateKey(), SignatureAlgorithm.RS256)
        .compact();
  }

  public String getEmailFromToken(String token) {
    return Jwts.parser()
        .verifyWith(keyProvider.getPublicKey()).build()
        .parseSignedClaims(token)
        .getPayload()
        .getSubject();
  }
}
