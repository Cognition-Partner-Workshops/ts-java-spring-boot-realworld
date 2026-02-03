package io.spring.api.shared;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.spring.core.user.User;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthenticationServiceTest {

  private AuthenticationService authenticationService;
  private SecurityContext originalContext;

  @BeforeEach
  public void setUp() {
    authenticationService = new AuthenticationService();
    originalContext = SecurityContextHolder.getContext();
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.setContext(originalContext);
  }

  @Test
  public void should_return_empty_when_no_authentication() {
    SecurityContext context = mock(SecurityContext.class);
    when(context.getAuthentication()).thenReturn(null);
    SecurityContextHolder.setContext(context);

    Optional<User> result = authenticationService.getCurrentUser();

    assertTrue(result.isEmpty());
  }

  @Test
  public void should_return_empty_when_anonymous_authentication() {
    SecurityContext context = mock(SecurityContext.class);
    AnonymousAuthenticationToken anonymousToken =
        new AnonymousAuthenticationToken(
            "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    when(context.getAuthentication()).thenReturn(anonymousToken);
    SecurityContextHolder.setContext(context);

    Optional<User> result = authenticationService.getCurrentUser();

    assertTrue(result.isEmpty());
  }

  @Test
  public void should_return_empty_when_principal_is_null() {
    SecurityContext context = mock(SecurityContext.class);
    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(null);
    when(context.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(context);

    Optional<User> result = authenticationService.getCurrentUser();

    assertTrue(result.isEmpty());
  }

  @Test
  public void should_return_user_when_authenticated() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    SecurityContext context = mock(SecurityContext.class);
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(user, null, AuthorityUtils.NO_AUTHORITIES);
    when(context.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(context);

    Optional<User> result = authenticationService.getCurrentUser();

    assertTrue(result.isPresent());
    assertEquals(user, result.get());
  }

  @Test
  public void should_return_true_when_authenticated() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    SecurityContext context = mock(SecurityContext.class);
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(user, null, AuthorityUtils.NO_AUTHORITIES);
    when(context.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(context);

    assertTrue(authenticationService.isAuthenticated());
  }

  @Test
  public void should_return_false_when_not_authenticated() {
    SecurityContext context = mock(SecurityContext.class);
    when(context.getAuthentication()).thenReturn(null);
    SecurityContextHolder.setContext(context);

    assertFalse(authenticationService.isAuthenticated());
  }

  @Test
  public void should_return_user_when_require_current_user_and_authenticated() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    SecurityContext context = mock(SecurityContext.class);
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(user, null, AuthorityUtils.NO_AUTHORITIES);
    when(context.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(context);

    User result = authenticationService.requireCurrentUser();

    assertEquals(user, result);
  }

  @Test
  public void should_throw_exception_when_require_current_user_and_not_authenticated() {
    SecurityContext context = mock(SecurityContext.class);
    when(context.getAuthentication()).thenReturn(null);
    SecurityContextHolder.setContext(context);

    assertThrows(
        AuthenticationRequiredException.class, () -> authenticationService.requireCurrentUser());
  }
}
