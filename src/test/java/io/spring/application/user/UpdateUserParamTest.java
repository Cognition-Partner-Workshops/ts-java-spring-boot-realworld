package io.spring.application.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class UpdateUserParamTest {

  @Test
  public void should_create_update_user_param_with_builder() {
    UpdateUserParam param =
        UpdateUserParam.builder()
            .email("test@example.com")
            .username("testuser")
            .password("password123")
            .bio("test bio")
            .image("https://example.com/image.jpg")
            .build();

    assertThat(param.getEmail(), is("test@example.com"));
    assertThat(param.getUsername(), is("testuser"));
    assertThat(param.getPassword(), is("password123"));
    assertThat(param.getBio(), is("test bio"));
    assertThat(param.getImage(), is("https://example.com/image.jpg"));
  }

  @Test
  public void should_create_update_user_param_with_default_values() {
    UpdateUserParam param = UpdateUserParam.builder().build();

    assertThat(param.getEmail(), is(""));
    assertThat(param.getUsername(), is(""));
    assertThat(param.getPassword(), is(""));
    assertThat(param.getBio(), is(""));
    assertThat(param.getImage(), is(""));
  }

  @Test
  public void should_create_update_user_param_with_no_args_constructor() {
    UpdateUserParam param = new UpdateUserParam();

    assertThat(param.getEmail(), is(""));
    assertThat(param.getUsername(), is(""));
    assertThat(param.getPassword(), is(""));
    assertThat(param.getBio(), is(""));
    assertThat(param.getImage(), is(""));
  }

  @Test
  public void should_create_update_user_param_with_all_args_constructor() {
    UpdateUserParam param =
        new UpdateUserParam(
            "test@example.com", "password123", "testuser", "test bio", "https://example.com/image.jpg");

    assertThat(param.getEmail(), is("test@example.com"));
    assertThat(param.getPassword(), is("password123"));
    assertThat(param.getUsername(), is("testuser"));
    assertThat(param.getBio(), is("test bio"));
    assertThat(param.getImage(), is("https://example.com/image.jpg"));
  }

  @Test
  public void should_create_update_user_param_with_partial_builder() {
    UpdateUserParam param = UpdateUserParam.builder().email("test@example.com").bio("new bio").build();

    assertThat(param.getEmail(), is("test@example.com"));
    assertThat(param.getUsername(), is(""));
    assertThat(param.getPassword(), is(""));
    assertThat(param.getBio(), is("new bio"));
    assertThat(param.getImage(), is(""));
  }
}
