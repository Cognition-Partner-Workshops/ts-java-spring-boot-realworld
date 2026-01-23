package io.spring.application.data;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class UserDataTest {

  @Test
  public void should_create_user_data_with_all_args_constructor() {
    UserData userData =
        new UserData("123", "test@example.com", "testuser", "test bio", "https://example.com/image.jpg");

    assertThat(userData.getId(), is("123"));
    assertThat(userData.getEmail(), is("test@example.com"));
    assertThat(userData.getUsername(), is("testuser"));
    assertThat(userData.getBio(), is("test bio"));
    assertThat(userData.getImage(), is("https://example.com/image.jpg"));
  }

  @Test
  public void should_create_user_data_with_no_args_constructor() {
    UserData userData = new UserData();

    assertThat(userData.getId(), is((String) null));
    assertThat(userData.getEmail(), is((String) null));
    assertThat(userData.getUsername(), is((String) null));
    assertThat(userData.getBio(), is((String) null));
    assertThat(userData.getImage(), is((String) null));
  }

  @Test
  public void should_set_and_get_id() {
    UserData userData = new UserData();
    userData.setId("456");

    assertThat(userData.getId(), is("456"));
  }

  @Test
  public void should_set_and_get_email() {
    UserData userData = new UserData();
    userData.setEmail("new@example.com");

    assertThat(userData.getEmail(), is("new@example.com"));
  }

  @Test
  public void should_set_and_get_username() {
    UserData userData = new UserData();
    userData.setUsername("newuser");

    assertThat(userData.getUsername(), is("newuser"));
  }

  @Test
  public void should_set_and_get_bio() {
    UserData userData = new UserData();
    userData.setBio("new bio");

    assertThat(userData.getBio(), is("new bio"));
  }

  @Test
  public void should_set_and_get_image() {
    UserData userData = new UserData();
    userData.setImage("https://example.com/newimage.jpg");

    assertThat(userData.getImage(), is("https://example.com/newimage.jpg"));
  }

  @Test
  public void should_have_equals_and_hashcode() {
    UserData userData1 =
        new UserData("123", "test@example.com", "testuser", "bio", "image");
    UserData userData2 =
        new UserData("123", "test@example.com", "testuser", "bio", "image");

    assertThat(userData1.equals(userData2), is(true));
    assertThat(userData1.hashCode(), is(userData2.hashCode()));
  }

  @Test
  public void should_have_to_string() {
    UserData userData =
        new UserData("123", "test@example.com", "testuser", "bio", "image");

    assertThat(userData.toString(), notNullValue());
  }
}
