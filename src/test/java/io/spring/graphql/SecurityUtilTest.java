package io.spring.graphql;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.spring.core.user.User;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
  public void should_return_empty_when_no_authentication() {
    SecurityContextHolder.getContext().setAuthentication(null);
    Optional<User> result = SecurityUtil.getCurrentUser();
    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void should_return_empty_when_anonymous_authentication() {
    AnonymousAuthenticationToken anonAuth = new AnonymousAuthenticationToken(
        "key", "anonymousUser", 
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
    SecurityContextHolder.getContext().setAuthentication(anonAuth);
    
    Optional<User> result = SecurityUtil.getCurrentUser();
    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void should_return_empty_when_null_principal() {
    UsernamePasswordAuthenticationToken auth = 
        new UsernamePasswordAuthenticationToken(null, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
    
    Optional<User> result = SecurityUtil.getCurrentUser();
    assertThat(result.isPresent(), is(false));
  }

  @Test
  public void should_return_user_when_authenticated() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    UsernamePasswordAuthenticationToken auth = 
        new UsernamePasswordAuthenticationToken(user, null);
    SecurityContextHolder.getContext().setAuthentication(auth);
    
    Optional<User> result = SecurityUtil.getCurrentUser();
    assertThat(result.isPresent(), is(true));
    assertThat(result.get(), is(user));
  }
}
