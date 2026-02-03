package io.spring.application.data;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class UserDataTest {

  @Test
  public void should_create_user_data_with_all_args_constructor() {
    UserData userData = new UserData("id", "email@example.com", "username", "bio", "image");

    assertThat(userData.getId(), is("id"));
    assertThat(userData.getEmail(), is("email@example.com"));
    assertThat(userData.getUsername(), is("username"));
    assertThat(userData.getBio(), is("bio"));
    assertThat(userData.getImage(), is("image"));
  }

  @Test
  public void should_create_user_data_with_no_args_constructor() {
    UserData userData = new UserData();
    
    assertThat(userData.getId(), is((String) null));
    assertThat(userData.getEmail(), is((String) null));
  }

  @Test
  public void should_set_and_get_properties() {
    UserData userData = new UserData();
    
    userData.setId("id");
    userData.setEmail("email@example.com");
    userData.setUsername("username");
    userData.setBio("bio");
    userData.setImage("image");

    assertThat(userData.getId(), is("id"));
    assertThat(userData.getEmail(), is("email@example.com"));
    assertThat(userData.getUsername(), is("username"));
    assertThat(userData.getBio(), is("bio"));
    assertThat(userData.getImage(), is("image"));
  }

  @Test
  public void should_implement_equals_and_hashcode() {
    UserData userData1 = new UserData("id", "email@example.com", "username", "bio", "image");
    UserData userData2 = new UserData("id", "email@example.com", "username", "bio", "image");
    UserData userData3 = new UserData("different-id", "email@example.com", "username", "bio", "image");

    assertThat(userData1.equals(userData2), is(true));
    assertThat(userData1.equals(userData3), is(false));
    assertThat(userData1.hashCode(), is(userData2.hashCode()));
    assertThat(userData1.hashCode(), is(not(userData3.hashCode())));
  }

  @Test
  public void should_implement_to_string() {
    UserData userData = new UserData();
    userData.setId("id");
    userData.setUsername("username");

    String toString = userData.toString();

    assertThat(toString, is(notNullValue()));
    assertThat(toString.contains("id"), is(true));
    assertThat(toString.contains("username"), is(true));
  }

  @Test
  public void should_handle_equals_with_null_and_different_type() {
    UserData userData = new UserData();
    userData.setId("id");

    assertThat(userData.equals(null), is(false));
    assertThat(userData.equals("string"), is(false));
    assertThat(userData.equals(userData), is(true));
  }

  @Test
  public void should_handle_equals_with_null_fields() {
    UserData userData1 = new UserData();
    UserData userData2 = new UserData();

    assertThat(userData1.equals(userData2), is(true));
    assertThat(userData1.hashCode(), is(userData2.hashCode()));
  }

  @Test
  public void should_handle_different_email() {
    UserData userData1 = new UserData("id", "email1@example.com", "username", "bio", "image");
    UserData userData2 = new UserData("id", "email2@example.com", "username", "bio", "image");

    assertThat(userData1.equals(userData2), is(false));
  }
}
