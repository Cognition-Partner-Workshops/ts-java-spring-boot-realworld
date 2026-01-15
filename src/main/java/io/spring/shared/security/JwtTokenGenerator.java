package io.spring.shared.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Shared JWT token generator that can be used across microservices. This component provides a
 * consistent way to generate JWT tokens across the distributed system.
 */
@Component
public class JwtTokenGenerator {

  private final SecretKey signingKey;
  private final SignatureAlgorithm signatureAlgorithm;
  private final int sessionTime;

  public JwtTokenGenerator(
      @Value("${jwt.secret}") String secret, @Value("${jwt.sessionTime}") int sessionTime) {
    this.sessionTime = sessionTime;
    this.signatureAlgorithm = SignatureAlgorithm.HS512;
    this.signingKey = new SecretKeySpec(secret.getBytes(), signatureAlgorithm.getJcaName());
  }

  /**
   * Generates a JWT token for the given user ID.
   *
   * @param userId the user ID to include in the token
   * @return the generated JWT token
   */
  public String generateToken(String userId) {
    return Jwts.builder()
        .setSubject(userId)
        .setIssuedAt(new Date())
        .setExpiration(expireTimeFromNow())
        .signWith(signingKey)
        .compact();
  }

  /**
   * Generates a JWT token with custom expiration time.
   *
   * @param userId the user ID to include in the token
   * @param expirationSeconds the expiration time in seconds
   * @return the generated JWT token
   */
  public String generateToken(String userId, int expirationSeconds) {
    return Jwts.builder()
        .setSubject(userId)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expirationSeconds * 1000L))
        .signWith(signingKey)
        .compact();
  }

  private Date expireTimeFromNow() {
    return new Date(System.currentTimeMillis() + sessionTime * 1000L);
  }
}
