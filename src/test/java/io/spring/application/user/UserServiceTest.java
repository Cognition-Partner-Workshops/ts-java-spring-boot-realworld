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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock private UserRepository userRepository;

  private UserService userService;
  private PasswordEncoder passwordEncoder;
  private String defaultImage;

  @BeforeEach
  public void setUp() {
    passwordEncoder = new BCryptPasswordEncoder();
    defaultImage = "https://static.productionready.io/images/smiley-cyrus.jpg";
    userService = new UserService(userRepository, defaultImage, passwordEncoder);
  }

  @Test
  public void should_create_user_with_encoded_password() {
    String email = "test@example.com";
    String username = "testuser";
    String password = "password123";

    RegisterParam registerParam = new RegisterParam(email, username, password);

    User createdUser = userService.createUser(registerParam);

    assertNotNull(createdUser);
    assertEquals(email, createdUser.getEmail());
    assertEquals(username, createdUser.getUsername());
    assertEquals(defaultImage, createdUser.getImage());
    assertEquals("", createdUser.getBio());
    assertTrue(passwordEncoder.matches(password, createdUser.getPassword()));

    verify(userRepository).save(any(User.class));
  }

  @Test
  public void should_create_user_with_default_image() {
    String email = "another@example.com";
    String username = "anotheruser";
    String password = "securepass";

    RegisterParam registerParam = new RegisterParam(email, username, password);

    User createdUser = userService.createUser(registerParam);

    assertEquals(defaultImage, createdUser.getImage());
  }

  @Test
  public void should_save_user_to_repository() {
    String email = "save@example.com";
    String username = "saveuser";
    String password = "savepass";

    RegisterParam registerParam = new RegisterParam(email, username, password);

    userService.createUser(registerParam);

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());

    User savedUser = userCaptor.getValue();
    assertEquals(email, savedUser.getEmail());
    assertEquals(username, savedUser.getUsername());
  }

  @Test
  public void should_update_user_email() {
    User existingUser = new User("old@example.com", "olduser", "oldpass", "old bio", "old image");
    UpdateUserParam updateParam = new UpdateUserParam("new@example.com", null, null, null, null);
    UpdateUserCommand command = new UpdateUserCommand(existingUser, updateParam);

    userService.updateUser(command);

    assertEquals("new@example.com", existingUser.getEmail());
    assertEquals("olduser", existingUser.getUsername());
    verify(userRepository).save(existingUser);
  }

  @Test
  public void should_update_user_username() {
    User existingUser = new User("test@example.com", "olduser", "pass", "bio", "image");
    UpdateUserParam updateParam =
        UpdateUserParam.builder().username("newuser").build();
    UpdateUserCommand command = new UpdateUserCommand(existingUser, updateParam);

    userService.updateUser(command);

    assertEquals("newuser", existingUser.getUsername());
    verify(userRepository).save(existingUser);
  }

  @Test
  public void should_update_user_bio() {
    User existingUser = new User("test@example.com", "user", "pass", "old bio", "image");
    UpdateUserParam updateParam = new UpdateUserParam(null, null, null, "new bio", null);
    UpdateUserCommand command = new UpdateUserCommand(existingUser, updateParam);

    userService.updateUser(command);

    assertEquals("new bio", existingUser.getBio());
    verify(userRepository).save(existingUser);
  }

  @Test
  public void should_update_user_image() {
    User existingUser = new User("test@example.com", "user", "pass", "bio", "old image");
    UpdateUserParam updateParam = new UpdateUserParam(null, null, null, null, "new image");
    UpdateUserCommand command = new UpdateUserCommand(existingUser, updateParam);

    userService.updateUser(command);

    assertEquals("new image", existingUser.getImage());
    verify(userRepository).save(existingUser);
  }

  @Test
  public void should_update_multiple_user_fields() {
    User existingUser = new User("old@example.com", "olduser", "oldpass", "old bio", "old image");
    UpdateUserParam updateParam =
        UpdateUserParam.builder()
            .email("new@example.com")
            .username("newuser")
            .password("newpass")
            .bio("new bio")
            .image("new image")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(existingUser, updateParam);

    userService.updateUser(command);

    assertEquals("new@example.com", existingUser.getEmail());
    assertEquals("newuser", existingUser.getUsername());
    assertEquals("new bio", existingUser.getBio());
    assertEquals("new image", existingUser.getImage());
    verify(userRepository).save(existingUser);
  }

  @Test
  public void should_not_update_fields_with_null_values() {
    User existingUser =
        new User("original@example.com", "originaluser", "originalpass", "original bio", "original image");
    UpdateUserParam updateParam = new UpdateUserParam(null, null, null, null, null);
    UpdateUserCommand command = new UpdateUserCommand(existingUser, updateParam);

    userService.updateUser(command);

    assertEquals("original@example.com", existingUser.getEmail());
    assertEquals("originaluser", existingUser.getUsername());
    assertEquals("original bio", existingUser.getBio());
    assertEquals("original image", existingUser.getImage());
    verify(userRepository).save(existingUser);
  }
}
