package io.spring.graphql;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.user.RegisterParam;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.CreateUserInput;
import io.spring.graphql.types.UpdateUserInput;
import io.spring.graphql.types.UserPayload;
import io.spring.graphql.types.UserResult;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserMutationTest {

  private UserRepository userRepository;
  private PasswordEncoder passwordEncoder;
  private UserService userService;
  private UserMutation userMutation;

  @BeforeEach
  public void setUp() {
    userRepository = mock(UserRepository.class);
    passwordEncoder = mock(PasswordEncoder.class);
    userService = mock(UserService.class);
    userMutation = new UserMutation(userRepository, passwordEncoder, userService);
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void should_create_user_successfully() {
    CreateUserInput input = CreateUserInput.newBuilder()
        .email("test@test.com")
        .username("testuser")
        .password("password")
        .build();

    User user = new User("test@test.com", "testuser", "encodedPassword", "", "");
    when(userService.createUser(any(RegisterParam.class))).thenReturn(user);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertThat(result, is(notNullValue()));
    assertThat(result.getData(), is(notNullValue()));
    assertThat(result.getLocalContext(), is(user));
  }

  @Test
  public void should_return_error_when_create_user_fails_validation() {
    CreateUserInput input = CreateUserInput.newBuilder()
        .email("invalid")
        .username("")
        .password("")
        .build();

    when(userService.createUser(any(RegisterParam.class)))
        .thenThrow(new ConstraintViolationException(new HashSet<ConstraintViolation<?>>()));

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertThat(result, is(notNullValue()));
  }

  @Test
  public void should_login_successfully() {
    User user = new User("test@test.com", "testuser", "encodedPassword", "", "");
    when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);

    DataFetcherResult<UserPayload> result = userMutation.login("password", "test@test.com");

    assertThat(result, is(notNullValue()));
    assertThat(result.getData(), is(notNullValue()));
    assertThat(result.getLocalContext(), is(user));
  }

  @Test
  public void should_throw_exception_when_login_with_wrong_password() {
    User user = new User("test@test.com", "testuser", "encodedPassword", "", "");
    when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

    try {
      userMutation.login("wrongpassword", "test@test.com");
      assertThat("Should have thrown exception", false);
    } catch (InvalidAuthenticationException e) {
      assertThat(e, is(notNullValue()));
    }
  }

  @Test
  public void should_throw_exception_when_login_with_nonexistent_email() {
    when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

    try {
      userMutation.login("password", "nonexistent@test.com");
      assertThat("Should have thrown exception", false);
    } catch (InvalidAuthenticationException e) {
      assertThat(e, is(notNullValue()));
    }
  }

  @Test
  public void should_return_null_when_update_user_with_anonymous_auth() {
    AnonymousAuthenticationToken anonAuth = new AnonymousAuthenticationToken(
        "key", "anonymousUser", 
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
    SecurityContextHolder.getContext().setAuthentication(anonAuth);

    UpdateUserInput input = UpdateUserInput.newBuilder()
        .username("newusername")
        .build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);

    assertThat(result, is(nullValue()));
  }

  @Test
  public void should_return_null_when_update_user_with_null_principal() {
    UsernamePasswordAuthenticationToken auth = 
        new UsernamePasswordAuthenticationToken(null, null);
    SecurityContextHolder.getContext().setAuthentication(auth);

    UpdateUserInput input = UpdateUserInput.newBuilder()
        .username("newusername")
        .build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);

    assertThat(result, is(nullValue()));
  }

  @Test
  public void should_update_user_successfully() {
    User user = new User("test@test.com", "testuser", "encodedPassword", "bio", "image");
    UsernamePasswordAuthenticationToken auth = 
        new UsernamePasswordAuthenticationToken(user, null);
    SecurityContextHolder.getContext().setAuthentication(auth);

    UpdateUserInput input = UpdateUserInput.newBuilder()
        .username("newusername")
        .email("new@test.com")
        .bio("new bio")
        .image("new image")
        .build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);

    assertThat(result, is(notNullValue()));
    assertThat(result.getData(), is(notNullValue()));
    assertThat(result.getLocalContext(), is(user));
  }
}
