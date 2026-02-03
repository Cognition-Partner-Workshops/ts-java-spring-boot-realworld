package io.spring.application.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  private UserService userService;

  @BeforeEach
  public void setUp() {
    userService = new UserService(userRepository, "default-image.png", passwordEncoder);
  }

  @Test
  public void should_create_user() {
    RegisterParam registerParam = new RegisterParam("test@test.com", "testuser", "password");
    when(passwordEncoder.encode("password")).thenReturn("encoded-password");

    User user = userService.createUser(registerParam);

    assertThat(user, is(notNullValue()));
    assertThat(user.getEmail(), is("test@test.com"));
    assertThat(user.getUsername(), is("testuser"));
    verify(userRepository).save(any(User.class));
  }

  @Test
  public void should_update_user() {
    User targetUser = new User("old@test.com", "olduser", "password", "bio", "image");
    UpdateUserParam updateParam = UpdateUserParam.builder()
        .email("new@test.com")
        .username("newuser")
        .bio("new bio")
        .image("new-image.png")
        .build();
    UpdateUserCommand command = new UpdateUserCommand(targetUser, updateParam);

    userService.updateUser(command);

    assertThat(targetUser.getEmail(), is("new@test.com"));
    assertThat(targetUser.getUsername(), is("newuser"));
    verify(userRepository).save(targetUser);
  }
}
