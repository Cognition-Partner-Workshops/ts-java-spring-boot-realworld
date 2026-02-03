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
  public void setUp() {
    userService = new UserService(userRepository, defaultImage, passwordEncoder);
  }

  @Test
  public void should_create_user_successfully() {
    RegisterParam registerParam = new RegisterParam("test@test.com", "testuser", "password123");
    when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

    User result = userService.createUser(registerParam);

    assertNotNull(result);
    assertEquals("test@test.com", result.getEmail());
    assertEquals("testuser", result.getUsername());
    assertEquals("encodedPassword", result.getPassword());
    assertEquals(defaultImage, result.getImage());
    verify(userRepository).save(any(User.class));
  }

  @Test
  public void should_update_user_successfully() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    UpdateUserParam updateParam =
        UpdateUserParam.builder()
            .email("new@test.com")
            .username("newuser")
            .bio("new bio")
            .image("new image")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(user, updateParam);

    userService.updateUser(command);

    verify(userRepository).save(user);
  }

  @Test
  public void should_update_user_with_partial_data() {
    User user = new User("test@test.com", "testuser", "password", "bio", "image");
    UpdateUserParam updateParam = UpdateUserParam.builder().bio("updated bio").build();
    UpdateUserCommand command = new UpdateUserCommand(user, updateParam);

    userService.updateUser(command);

    verify(userRepository).save(user);
  }
}
