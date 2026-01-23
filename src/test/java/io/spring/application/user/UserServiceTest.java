package io.spring.application.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import io.spring.core.user.User;
import io.spring.core.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock private UserRepository userRepository;

  private UserService userService;
  private PasswordEncoder passwordEncoder;
  private String defaultImage = "https://static.productionready.io/images/smiley-cyrus.jpg";

  @BeforeEach
  public void setUp() {
    passwordEncoder = new BCryptPasswordEncoder();
    userService = new UserService(userRepository, defaultImage, passwordEncoder);
  }

  @Test
  public void should_create_user_with_encoded_password() {
    RegisterParam registerParam = new RegisterParam("test@example.com", "testuser", "password123");

    User user = userService.createUser(registerParam);

    assertThat(user, notNullValue());
    assertThat(user.getEmail(), is("test@example.com"));
    assertThat(user.getUsername(), is("testuser"));
    assertThat(user.getImage(), is(defaultImage));
    assertThat(user.getBio(), is(""));
    assertThat(passwordEncoder.matches("password123", user.getPassword()), is(true));
    verify(userRepository).save(any(User.class));
  }

  @Test
  public void should_update_user_profile() {
    User user = new User("old@example.com", "olduser", "oldpass", "oldbio", "oldimage");
    UpdateUserParam updateParam =
        UpdateUserParam.builder()
            .email("new@example.com")
            .username("newuser")
            .bio("newbio")
            .image("newimage")
            .build();
    UpdateUserCommand command = new UpdateUserCommand(user, updateParam);

    userService.updateUser(command);

    assertThat(user.getEmail(), is("new@example.com"));
    assertThat(user.getUsername(), is("newuser"));
    assertThat(user.getBio(), is("newbio"));
    assertThat(user.getImage(), is("newimage"));
    verify(userRepository).save(user);
  }

  @Test
  public void should_update_user_with_password() {
    User user = new User("test@example.com", "testuser", "oldpass", "bio", "image");
    UpdateUserParam updateParam = UpdateUserParam.builder().password("newpassword").build();
    UpdateUserCommand command = new UpdateUserCommand(user, updateParam);

    userService.updateUser(command);

    assertThat(user.getPassword(), is("newpassword"));
    verify(userRepository).save(user);
  }
}
