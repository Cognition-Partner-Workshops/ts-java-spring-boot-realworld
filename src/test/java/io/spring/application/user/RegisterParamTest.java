package io.spring.application.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class RegisterParamTest {

  @Test
  public void should_create_register_param_with_all_args_constructor() {
    RegisterParam param = new RegisterParam("test@example.com", "testuser", "password123");

    assertThat(param.getEmail(), is("test@example.com"));
    assertThat(param.getUsername(), is("testuser"));
    assertThat(param.getPassword(), is("password123"));
  }

  @Test
  public void should_create_register_param_with_no_args_constructor() {
    RegisterParam param = new RegisterParam();

    assertThat(param.getEmail(), is((String) null));
    assertThat(param.getUsername(), is((String) null));
    assertThat(param.getPassword(), is((String) null));
  }
}
