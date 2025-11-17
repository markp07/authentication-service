package nl.markpost.demo.authentication.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import nl.markpost.demo.authentication.constant.Constants;
import nl.markpost.demo.authentication.model.User;
import nl.markpost.demo.authentication.security.JwtKeyProvider;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

  private final JwtKeyProvider keyProvider;

  public String generateAccessToken(User user) {
    long minutes15 = Constants.MINUTES_15 * 1000;
    long expirationTime = System.currentTimeMillis() + minutes15;
    return Jwts.builder()
        .subject(user.getEmail())
        .claim("userId", user.getId().toString())
        .issuedAt(new Date())
        .expiration(new Date(expirationTime))
        .signWith(keyProvider.getPrivateKey(), SignatureAlgorithm.RS256)
        .compact();
  }

  public String generateRefreshToken(User user) {
    long days7 = Constants.DAYS_7 * 1000;
    long expirationTime = System.currentTimeMillis() + days7;
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
