package io.spring.infrastructure.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.spring.core.service.JwtService;
import io.spring.core.user.User;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DefaultJwtServiceTest {
  // >= 64 bytes so the HMAC key selects HS512, like the configured jwt.secret (86 bytes)
  private static final String SECRET =
      "1231231231231231231231231231231231231231231231231231231231231234";
  private static final String OTHER_SECRET =
      "4564564564564564564564564564564564564564564564564564564564564567";
  private static final int SESSION_TIME = 3600;

  private JwtService jwtService;
  private User user;

  @BeforeEach
  public void setUp() {
    jwtService = new DefaultJwtService(SECRET, SESSION_TIME);
    user = new User("email@email.com", "username", "123", "", "");
  }

  @Test
  public void should_generate_and_parse_token() {
    String token = jwtService.toToken(user);
    Assertions.assertNotNull(token);
    Optional<String> optional = jwtService.getSubFromToken(token);
    Assertions.assertTrue(optional.isPresent());
    Assertions.assertEquals(optional.get(), user.getId());
  }

  @Test
  public void should_sign_with_hs512_subject_user_id_and_expiry_now_plus_session_time() {
    long before = System.currentTimeMillis();
    String token = jwtService.toToken(user);
    long after = System.currentTimeMillis();

    String header =
        new String(Base64.getUrlDecoder().decode(token.split("\\.")[0]), StandardCharsets.UTF_8);
    Assertions.assertTrue(header.contains("\"alg\":\"HS512\""), header);

    Claims claims =
        Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
            .build()
            .parseSignedClaims(token)
            .getPayload();
    Assertions.assertEquals(user.getId(), claims.getSubject());
    long expiry = claims.getExpiration().getTime();
    // exp is stored with second precision
    Assertions.assertTrue(expiry >= (before / 1000) * 1000 + SESSION_TIME * 1000L);
    Assertions.assertTrue(expiry <= after + SESSION_TIME * 1000L);
  }

  @Test
  public void should_get_null_with_wrong_jwt() {
    Optional<String> optional = jwtService.getSubFromToken("123");
    Assertions.assertFalse(optional.isPresent());
  }

  @Test
  public void should_get_null_with_null_or_empty_token() {
    Assertions.assertFalse(jwtService.getSubFromToken(null).isPresent());
    Assertions.assertFalse(jwtService.getSubFromToken("").isPresent());
  }

  @Test
  public void should_get_null_with_expired_jwt() {
    String token =
        "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhaXNlbnNpeSIsImV4cCI6MTUwMjE2MTIwNH0.SJB-U60WzxLYNomqLo4G3v3LzFxJKuVrIud8D8Lz3-mgpo9pN1i7C8ikU_jQPJGm8HsC1CquGMI-rSuM7j6LDA";
    Assertions.assertFalse(jwtService.getSubFromToken(token).isPresent());
  }

  @Test
  public void should_get_null_with_freshly_signed_but_expired_jwt() {
    String token =
        Jwts.builder()
            .subject(user.getId())
            .expiration(new Date(System.currentTimeMillis() - 60_000))
            .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
            .compact();

    Assertions.assertFalse(jwtService.getSubFromToken(token).isPresent());
  }

  @Test
  public void should_get_null_with_token_signed_by_another_key() {
    String token = new DefaultJwtService(OTHER_SECRET, SESSION_TIME).toToken(user);

    Assertions.assertFalse(jwtService.getSubFromToken(token).isPresent());
  }

  @Test
  public void should_get_null_with_tampered_payload() {
    String token = jwtService.toToken(user);
    String[] parts = token.split("\\.");
    String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
    String tamperedPayload =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(
                payload.replace(user.getId(), "someone-else").getBytes(StandardCharsets.UTF_8));

    Assertions.assertFalse(
        jwtService.getSubFromToken(parts[0] + "." + tamperedPayload + "." + parts[2]).isPresent());
  }

  @Test
  public void should_get_null_with_tampered_signature() {
    String token = jwtService.toToken(user);
    String[] parts = token.split("\\.");
    // flip a character in the middle: the final base64url char only carries 2 significant bits,
    // so changing it may decode to the very same signature bytes
    int index = parts[2].length() / 2;
    char original = parts[2].charAt(index);
    String tamperedSignature =
        parts[2].substring(0, index)
            + (original == 'A' ? 'B' : 'A')
            + parts[2].substring(index + 1);

    Assertions.assertFalse(
        jwtService
            .getSubFromToken(parts[0] + "." + parts[1] + "." + tamperedSignature)
            .isPresent());
  }

  @Test
  public void should_get_null_with_unsigned_token() {
    String token = jwtService.toToken(user);
    String[] parts = token.split("\\.");
    String noneHeader =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString("{\"alg\":\"none\"}".getBytes(StandardCharsets.UTF_8));

    Assertions.assertFalse(
        jwtService.getSubFromToken(noneHeader + "." + parts[1] + ".").isPresent());
  }
}
