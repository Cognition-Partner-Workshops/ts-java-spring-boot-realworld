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

  @BeforeEach
  public void setUp() {
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void should_return_user_with_bio_and_image() {
    User user = new User("test2@test.com", "testuser2", "password", "my bio", "my image");
    UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(user, null, AuthorityUtils.createAuthorityList("ROLE_USER"));
    SecurityContextHolder.getContext().setAuthentication(authToken);

    Optional<User> result = SecurityUtil.getCurrentUser();
    assertTrue(result.isPresent());
    assertEquals("my bio", result.get().getBio());
    assertEquals("my image", result.get().getImage());
  }

  @Test
  public void should_return_empty_when_anonymous_authentication() {
    AnonymousAuthenticationToken anonymousToken =
        new AnonymousAuthenticationToken(
            "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    SecurityContextHolder.getContext().setAuthentication(anonymousToken);

    Optional<User> result = SecurityUtil.getCurrentUser();
    assertTrue(result.isEmpty());
  }

  @Test
  public void should_return_user_when_authenticated() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(user, null, AuthorityUtils.createAuthorityList("ROLE_USER"));
    SecurityContextHolder.getContext().setAuthentication(authToken);

    Optional<User> result = SecurityUtil.getCurrentUser();
    assertTrue(result.isPresent());
    assertEquals("testuser", result.get().getUsername());
    assertEquals("test@test.com", result.get().getEmail());
  }

  @Test
  public void should_return_empty_when_principal_is_null() {
    UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(null, null, AuthorityUtils.createAuthorityList("ROLE_USER"));
    SecurityContextHolder.getContext().setAuthentication(authToken);

    Optional<User> result = SecurityUtil.getCurrentUser();
    assertTrue(result.isEmpty());
  }
}
