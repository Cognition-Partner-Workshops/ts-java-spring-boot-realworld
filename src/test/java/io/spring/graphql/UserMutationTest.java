package io.spring.graphql;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
import java.util.Optional;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserMutationTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private UserService userService;

  private UserMutation userMutation;
  private User user;

  @BeforeEach
  void setUp() {
    userMutation = new UserMutation(userRepository, passwordEncoder, userService);
    user = new User("test@example.com", "testuser", "password123", "bio", "image.jpg");
    SecurityContextHolder.clearContext();
  }

  @Test
  void createUser_success() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("test@example.com")
            .username("testuser")
            .password("password123")
            .build();

    when(userService.createUser(any(RegisterParam.class))).thenReturn(user);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(user, result.getLocalContext());
    verify(userService).createUser(any(RegisterParam.class));
  }

  @Test
  void createUser_withConstraintViolation() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("invalid")
            .username("testuser")
            .password("password123")
            .build();

    ConstraintViolationException cve = mock(ConstraintViolationException.class);
    when(cve.getConstraintViolations()).thenReturn(java.util.Collections.emptySet());
    when(userService.createUser(any(RegisterParam.class))).thenThrow(cve);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void login_success() {
    String email = "test@example.com";
    String password = "password123";

    when(userRepository.findByEmail(eq(email))).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(eq(password), eq(user.getPassword()))).thenReturn(true);

    DataFetcherResult<UserPayload> result = userMutation.login(password, email);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(user, result.getLocalContext());
  }

  @Test
  void login_invalidCredentials() {
    String email = "test@example.com";
    String password = "wrongpassword";

    when(userRepository.findByEmail(eq(email))).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(eq(password), eq(user.getPassword()))).thenReturn(false);

    assertThrows(InvalidAuthenticationException.class, () -> userMutation.login(password, email));
  }

  @Test
  void login_userNotFound() {
    String email = "nonexistent@example.com";
    String password = "password123";

    when(userRepository.findByEmail(eq(email))).thenReturn(Optional.empty());

    assertThrows(InvalidAuthenticationException.class, () -> userMutation.login(password, email));
  }

  @Test
  void updateUser_success() {
    UpdateUserInput input =
        UpdateUserInput.newBuilder()
            .email("new@example.com")
            .username("newusername")
            .bio("new bio")
            .image("new-image.jpg")
            .password("newpassword")
            .build();

    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(user, null, java.util.Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(user, result.getLocalContext());
    verify(userService).updateUser(any());
  }

  @Test
  void updateUser_withAnonymousUser() {
    UpdateUserInput input = UpdateUserInput.newBuilder().email("new@example.com").build();

    org.springframework.security.authentication.AnonymousAuthenticationToken auth =
        new org.springframework.security.authentication.AnonymousAuthenticationToken(
            "key",
            "anonymousUser",
            java.util.Collections.singletonList(
                new org.springframework.security.core.authority.SimpleGrantedAuthority(
                    "ROLE_ANONYMOUS")));
    SecurityContextHolder.getContext().setAuthentication(auth);

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);

    assertNull(result);
  }
}
