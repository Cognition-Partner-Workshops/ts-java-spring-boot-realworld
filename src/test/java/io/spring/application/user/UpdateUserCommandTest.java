package io.spring.application.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import io.spring.core.user.User;
import org.junit.jupiter.api.Test;

public class UpdateUserCommandTest {

  @Test
  public void should_create_update_user_command() {
    User user = new User("test@example.com", "testuser", "password", "bio", "image");
    UpdateUserParam param =
        UpdateUserParam.builder().email("new@example.com").username("newuser").build();

    UpdateUserCommand command = new UpdateUserCommand(user, param);

    assertThat(command.getTargetUser(), is(user));
    assertThat(command.getParam(), is(param));
  }
}
