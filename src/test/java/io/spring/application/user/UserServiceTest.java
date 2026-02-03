package io.spring.application.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  private UserService userService;

  private static final String DEFAULT_IMAGE = "https://example.com/default.png";

  @BeforeEach
  public void setUp() {
    userService = new UserService(userRepository, DEFAULT_IMAGE, passwordEncoder);
  }

  @Test
  public void should_create_user_with_encoded_password() {
    RegisterParam registerParam = new RegisterParam("test@email.com", "testuser", "password123");
    when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

    User createdUser = userService.createUser(registerParam);

    assertNotNull(createdUser);
    assertEquals("test@email.com", createdUser.getEmail());
    assertEquals("testuser", createdUser.getUsername());
    assertEquals("encodedPassword", createdUser.getPassword());
    assertEquals("", createdUser.getBio());
    assertEquals(DEFAULT_IMAGE, createdUser.getImage());

    verify(userRepository).save(any(User.class));
    verify(passwordEncoder).encode("password123");
  }

  @Test
  public void should_save_user_to_repository() {
    RegisterParam registerParam = new RegisterParam("test@email.com", "testuser", "password123");
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

    userService.createUser(registerParam);

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());

    User savedUser = userCaptor.getValue();
    assertEquals("test@email.com", savedUser.getEmail());
    assertEquals("testuser", savedUser.getUsername());
  }

  @Test
  public void should_update_user_with_new_values() {
    User existingUser = new User("old@email.com", "olduser", "oldpass", "old bio", "old.jpg");
    UpdateUserParam updateParam =
        UpdateUserParam.builder()
            .email("new@email.com")
            .username("newuser")
            .bio("new bio")
            .image("new.jpg")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(existingUser, updateParam);

    userService.updateUser(command);

    assertEquals("new@email.com", existingUser.getEmail());
    assertEquals("newuser", existingUser.getUsername());
    assertEquals("new bio", existingUser.getBio());
    assertEquals("new.jpg", existingUser.getImage());

    verify(userRepository).save(existingUser);
  }

  @Test
  public void should_not_update_fields_with_empty_values() {
    User existingUser = new User("old@email.com", "olduser", "oldpass", "old bio", "old.jpg");
    UpdateUserParam updateParam = UpdateUserParam.builder().build();
    UpdateUserCommand command = new UpdateUserCommand(existingUser, updateParam);

    userService.updateUser(command);

    assertEquals("old@email.com", existingUser.getEmail());
    assertEquals("olduser", existingUser.getUsername());
    assertEquals("old bio", existingUser.getBio());
    assertEquals("old.jpg", existingUser.getImage());

    verify(userRepository).save(existingUser);
  }

  @Test
  public void should_update_only_specified_fields() {
    User existingUser = new User("old@email.com", "olduser", "oldpass", "old bio", "old.jpg");
    UpdateUserParam updateParam = UpdateUserParam.builder().bio("new bio").build();
    UpdateUserCommand command = new UpdateUserCommand(existingUser, updateParam);

    userService.updateUser(command);

    assertEquals("old@email.com", existingUser.getEmail());
    assertEquals("olduser", existingUser.getUsername());
    assertEquals("new bio", existingUser.getBio());
    assertEquals("old.jpg", existingUser.getImage());
  }
}
