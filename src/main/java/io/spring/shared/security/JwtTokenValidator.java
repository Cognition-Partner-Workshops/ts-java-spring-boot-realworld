package io.spring.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.util.Optional;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Shared JWT token validator that can be used across microservices. This component extracts JWT
 * validation logic without requiring direct database access, making it suitable for distributed
 * microservices architecture.
 */
@Component
public class JwtTokenValidator {

  private final SecretKey signingKey;

  public JwtTokenValidator(@Value("${jwt.secret}") String secret) {
    this.signingKey = new SecretKeySpec(secret.getBytes(), "HmacSHA512");
  }

  /**
   * Validates a JWT token and extracts the subject (user ID).
   *
   * @param token the JWT token to validate
   * @return Optional containing the user ID if token is valid, empty otherwise
   */
  public Optional<String> validateToken(String token) {
    try {
      Jws<Claims> claimsJws =
          Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token);
      return Optional.ofNullable(claimsJws.getBody().getSubject());
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  /**
   * Extracts the token from the Authorization header.
   *
   * @param authorizationHeader the Authorization header value
   * @return Optional containing the token if present, empty otherwise
   */
  public Optional<String> extractToken(String authorizationHeader) {
    if (authorizationHeader == null) {
      return Optional.empty();
    }
    String[] parts = authorizationHeader.split(" ");
    if (parts.length < 2) {
      return Optional.empty();
    }
    return Optional.ofNullable(parts[1]);
  }

  /**
   * Validates the Authorization header and extracts the user ID.
   *
   * @param authorizationHeader the Authorization header value
   * @return Optional containing the user ID if token is valid, empty otherwise
   */
  public Optional<String> validateAuthorizationHeader(String authorizationHeader) {
    return extractToken(authorizationHeader).flatMap(this::validateToken);
  }
}
