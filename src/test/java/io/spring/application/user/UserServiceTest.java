package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  private UserService userService;

  private String defaultImage = "https://static.productionready.io/images/smiley-cyrus.jpg";

  @BeforeEach
  void setUp() {
    userService = new UserService(userRepository, defaultImage, passwordEncoder);
  }

  @Test
  void createUser_success() {
    RegisterParam param = new RegisterParam("test@example.com", "testuser", "password123");

    when(passwordEncoder.encode(eq("password123"))).thenReturn("encodedPassword");

    User result = userService.createUser(param);

    assertNotNull(result);
    assertEquals("test@example.com", result.getEmail());
    assertEquals("testuser", result.getUsername());
    assertEquals("encodedPassword", result.getPassword());
    assertEquals(defaultImage, result.getImage());
    verify(userRepository).save(any(User.class));
    verify(passwordEncoder).encode(eq("password123"));
  }

  @Test
  void updateUser_success() {
    User existingUser = new User("old@example.com", "olduser", "oldPassword", "old bio", "old image");
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("new@example.com")
            .username("newuser")
            .bio("new bio")
            .image("new image")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(existingUser, param);

    userService.updateUser(command);

    assertEquals("new@example.com", existingUser.getEmail());
    assertEquals("newuser", existingUser.getUsername());
    assertEquals("new bio", existingUser.getBio());
    assertEquals("new image", existingUser.getImage());
    verify(userRepository).save(eq(existingUser));
  }

  @Test
  void updateUser_withPartialUpdate() {
    User existingUser = new User("old@example.com", "olduser", "oldPassword", "old bio", "old image");
    UpdateUserParam param = UpdateUserParam.builder().bio("new bio only").build();
    UpdateUserCommand command = new UpdateUserCommand(existingUser, param);

    userService.updateUser(command);

    assertEquals("new bio only", existingUser.getBio());
    verify(userRepository).save(eq(existingUser));
  }

  @Test
  void updateUser_withPassword() {
    User existingUser = new User("old@example.com", "olduser", "oldPassword", "old bio", "old image");
    UpdateUserParam param = UpdateUserParam.builder().password("newPassword").build();
    UpdateUserCommand command = new UpdateUserCommand(existingUser, param);

    userService.updateUser(command);

    verify(userRepository).save(eq(existingUser));
  }
}
