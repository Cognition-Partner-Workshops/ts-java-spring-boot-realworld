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
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import javax.validation.ConstraintViolation;
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
public class UserMutationTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private UserService userService;

  private UserMutation userMutation;

  private User testUser;

  @BeforeEach
  void setUp() {
    userMutation = new UserMutation(userRepository, passwordEncoder, userService);
    testUser = new User("test@example.com", "testuser", "encodedPassword", "bio", "image");
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

    when(userService.createUser(any(RegisterParam.class))).thenReturn(testUser);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(testUser, result.getLocalContext());
    verify(userService).createUser(any(RegisterParam.class));
  }

  @Test
  void createUser_withConstraintViolation() {
    CreateUserInput input =
        CreateUserInput.newBuilder()
            .email("invalid")
            .username("")
            .password("password123")
            .build();

    Set<ConstraintViolation<?>> violations = new HashSet<>();
    ConstraintViolationException cve = new ConstraintViolationException("Validation failed", violations);
    when(userService.createUser(any(RegisterParam.class))).thenThrow(cve);

    DataFetcherResult<UserResult> result = userMutation.createUser(input);

    assertNotNull(result);
    assertNotNull(result.getData());
  }

  @Test
  void login_success() {
    String email = "test@example.com";
    String password = "password123";

    when(userRepository.findByEmail(eq(email))).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(eq(password), eq(testUser.getPassword()))).thenReturn(true);

    DataFetcherResult<UserPayload> result = userMutation.login(password, email);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(testUser, result.getLocalContext());
  }

  @Test
  void login_invalidCredentials() {
    String email = "test@example.com";
    String password = "wrongpassword";

    when(userRepository.findByEmail(eq(email))).thenReturn(Optional.of(testUser));
    when(passwordEncoder.matches(eq(password), eq(testUser.getPassword()))).thenReturn(false);

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
            .email("updated@example.com")
            .username("updateduser")
            .bio("new bio")
            .image("new image")
            .build();

    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(testUser, null));

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);

    assertNotNull(result);
    assertNotNull(result.getData());
    assertEquals(testUser, result.getLocalContext());
    verify(userService).updateUser(any());
  }

  @Test
  void updateUser_notAuthenticated() {
    UpdateUserInput input =
        UpdateUserInput.newBuilder().email("updated@example.com").username("updateduser").build();

    SecurityContextHolder.clearContext();

    DataFetcherResult<UserPayload> result = userMutation.updateUser(input);

    assertNull(result);
    verify(userService, never()).updateUser(any());
  }
}
