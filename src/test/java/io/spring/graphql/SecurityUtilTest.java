package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;

import io.spring.core.user.User;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtilTest {

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = new User("test@example.com", "testuser", "password", "bio", "image");
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void getCurrentUser_withAuthenticatedUser() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(testUser, null));

    Optional<User> result = SecurityUtil.getCurrentUser();

    assertTrue(result.isPresent());
    assertEquals(testUser, result.get());
  }

  @Test
  void getCurrentUser_withAnonymousAuthentication() {
    AnonymousAuthenticationToken anonymousToken =
        new AnonymousAuthenticationToken(
            "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    SecurityContextHolder.getContext().setAuthentication(anonymousToken);

    Optional<User> result = SecurityUtil.getCurrentUser();

    assertFalse(result.isPresent());
  }

  @Test
  void getCurrentUser_withNullPrincipal() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(null, null));

    Optional<User> result = SecurityUtil.getCurrentUser();

    assertFalse(result.isPresent());
  }

  @Test
  void getCurrentUser_withNoAuthentication() {
    SecurityContextHolder.clearContext();

    Optional<User> result = SecurityUtil.getCurrentUser();

    assertFalse(result.isPresent());
  }
}
