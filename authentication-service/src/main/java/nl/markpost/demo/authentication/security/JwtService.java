package nl.markpost.demo.authentication.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Date;
import nl.markpost.demo.authentication.constant.Constants;
import nl.markpost.demo.authentication.model.User;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final JwtKeyProvider keyProvider;

  @Autowired
  public JwtService(JwtKeyProvider keyProvider) {
    this.keyProvider = keyProvider;
  }

  public String generateAccessToken(User user) {
    return Jwts.builder()
        .setSubject(user.getEmail())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + Constants.MINUTES_15))
        .signWith(keyProvider.getPrivateKey(), SignatureAlgorithm.RS256)
        .compact();
  }

  public String generateRefreshToken(User user) {
    return Jwts.builder()
        .setSubject(user.getEmail())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + Constants.DAYS_7))
        .signWith(keyProvider.getPrivateKey(), SignatureAlgorithm.RS256)
        .compact();
  }

  public String getEmailFromToken(String token) {
    return Jwts.parserBuilder().setSigningKey(keyProvider.getPublicKey()).build()
        .parseClaimsJws(token).getBody().getSubject();
  }
}
