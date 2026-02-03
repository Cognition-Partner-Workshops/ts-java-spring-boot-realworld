package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import graphql.execution.DataFetcherResult;
import io.spring.api.exception.InvalidAuthenticationException;
import io.spring.application.user.UserService;
import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import io.spring.graphql.types.CreateUserInput;
import io.spring.graphql.types.UpdateUserInput;
import io.spring.graphql.types.UserPayload;
import io.spring.graphql.types.UserResult;
import java.util.Optional;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserMutationTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder encryptService;
  @Mock private UserService userService;

  private UserMutation userMutation;

  @BeforeEach
  public void setUp() {
    userMutation = new UserMutation(userRepository, encryptService, userService);
    SecurityContextHolder.clearContext();
  }

  @AfterEach
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void should_create_user_successfully() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("test@test.com")
            .username("testuser")
            .password("password123")
            .build();

    User user = new User("test@test.com", "testuser", "encodedPassword", "", "");
    when(userService.createUser(any())).thenReturn(user);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(user, result.getLocalContext());
    verify(userService).createUser(any());
  }

  @Test
  public void should_create_user_with_different_credentials() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("another@test.com")
            .username("anotheruser")
            .password("password456")
            .build();

    User user = new User("another@test.com", "anotheruser", "encodedPassword", "", "");
    when(userService.createUser(any())).thenReturn(user);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    verify(userService).createUser(any());
  }

  @Test
  public void should_login_successfully() {
    String email = "test@test.com";
    String password = "password123";
    String encodedPassword = "encodedPassword";

    User user = new User(email, "testuser", encodedPassword, "", "");
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(encryptService.matches(password, encodedPassword)).thenReturn(true);

    DataFetcherResult<UserPayload> result = userMutation.login(password, email);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(user, result.getLocalContext());
  }

  @Test
  public void should_throw_exception_when_login_with_wrong_password() {
    String email = "test@test.com";
    String password = "wrongPassword";
    String encodedPassword = "encodedPassword";

    User user = new User(email, "testuser", encodedPassword, "", "");
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(encryptService.matches(password, encodedPassword)).thenReturn(false);

    assertThrows(InvalidAuthenticationException.class, () -> userMutation.login(password, email));
  }

  @Test
  public void should_throw_exception_when_user_not_found() {
    String email = "nonexistent@test.com";
    String password = "password123";

    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    assertThrows(InvalidAuthenticationException.class, () -> userMutation.login(password, email));
  }

  @Test
  public void should_update_user_successfully() {
    User currentUser = new User("test@test.com", "testuser", "password", "bio", "image");
    UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(
            currentUser, null, AuthorityUtils.createAuthorityList("ROLE_USER"));
    SecurityContextHolder.getContext().setAuthentication(authToken);

    UpdateUserInput updateInput =
        UpdateUserInput.newBuilder()
            .username("newUsername")
            .email("new@test.com")
            .bio("new bio")
            .image("newImage")
            .build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(updateInput);

    assertNotNull(result);
    assertNotNull(result.getData());
    verify(userService).updateUser(any());
  }

  @Test
  public void should_update_user_with_partial_data() {
    User currentUser = new User("test@test.com", "testuser", "password", "bio", "image");
    UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(
            currentUser, null, AuthorityUtils.createAuthorityList("ROLE_USER"));
    SecurityContextHolder.getContext().setAuthentication(authToken);

    UpdateUserInput updateInput =
        UpdateUserInput.newBuilder()
            .bio("updated bio")
            .build();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(updateInput);

    assertNotNull(result);
    verify(userService).updateUser(any());
  }
}
