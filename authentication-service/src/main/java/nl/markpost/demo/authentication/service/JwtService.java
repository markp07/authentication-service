package nl.markpost.demo.authentication.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import nl.markpost.demo.authentication.security.JwtKeyProvider;
import java.util.Date;
import nl.markpost.demo.authentication.constant.Constants;
import nl.markpost.demo.authentication.model.User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

  private final JwtKeyProvider keyProvider;

  public String generateAccessToken(User user) {
    long minutes15 = Constants.MINUTES_15 * 1000;
    long expirationTime = System.currentTimeMillis() + minutes15;
    return Jwts.builder()
        .setSubject(user.getEmail())
        .setIssuedAt(new Date())
        .setExpiration(new Date(expirationTime))
        .signWith(keyProvider.getPrivateKey(), SignatureAlgorithm.RS256)
        .compact();
  }

  public String generateRefreshToken(User user) {
    long days7 = Constants.DAYS_7 * 1000;
    long expirationTime = System.currentTimeMillis() + days7;
    return Jwts.builder()
        .setSubject(user.getEmail())
        .setIssuedAt(new Date())
        .setExpiration(new Date(expirationTime))
        .signWith(keyProvider.getPrivateKey(), SignatureAlgorithm.RS256)
        .compact();
  }

  public String getEmailFromToken(String token) {
    return Jwts.parserBuilder().setSigningKey(keyProvider.getPublicKey()).build()
        .parseClaimsJws(token).getBody().getSubject();
  }
}
