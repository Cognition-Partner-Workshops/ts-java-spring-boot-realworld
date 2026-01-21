package io.spring.core.user;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class UserTest {

  @Test
  public void should_create_user_with_all_fields() {
    User user = new User("test@example.com", "testuser", "password123", "My bio", "http://image.url");
    
    assertThat(user.getId(), notNullValue());
    assertThat(user.getEmail(), is("test@example.com"));
    assertThat(user.getUsername(), is("testuser"));
    assertThat(user.getPassword(), is("password123"));
    assertThat(user.getBio(), is("My bio"));
    assertThat(user.getImage(), is("http://image.url"));
  }

  @Test
  public void should_generate_unique_id_for_each_user() {
    User user1 = new User("user1@example.com", "user1", "pass1", "bio1", "image1");
    User user2 = new User("user2@example.com", "user2", "pass2", "bio2", "image2");
    
    assertThat(user1.getId(), not(user2.getId()));
  }

  @Test
  public void should_update_email_when_not_empty() {
    User user = new User("old@example.com", "testuser", "password", "bio", "image");
    user.update("new@example.com", "", "", "", "");
    
    assertThat(user.getEmail(), is("new@example.com"));
    assertThat(user.getUsername(), is("testuser"));
  }

  @Test
  public void should_update_username_when_not_empty() {
    User user = new User("test@example.com", "olduser", "password", "bio", "image");
    user.update("", "newuser", "", "", "");
    
    assertThat(user.getUsername(), is("newuser"));
    assertThat(user.getEmail(), is("test@example.com"));
  }

  @Test
  public void should_update_password_when_not_empty() {
    User user = new User("test@example.com", "testuser", "oldpassword", "bio", "image");
    user.update("", "", "newpassword", "", "");
    
    assertThat(user.getPassword(), is("newpassword"));
  }

  @Test
  public void should_update_bio_when_not_empty() {
    User user = new User("test@example.com", "testuser", "password", "old bio", "image");
    user.update("", "", "", "new bio", "");
    
    assertThat(user.getBio(), is("new bio"));
  }

  @Test
  public void should_update_image_when_not_empty() {
    User user = new User("test@example.com", "testuser", "password", "bio", "http://old.image");
    user.update("", "", "", "", "http://new.image");
    
    assertThat(user.getImage(), is("http://new.image"));
  }

  @Test
  public void should_update_multiple_fields_at_once() {
    User user = new User("old@example.com", "olduser", "oldpass", "old bio", "http://old.image");
    user.update("new@example.com", "newuser", "newpass", "new bio", "http://new.image");
    
    assertThat(user.getEmail(), is("new@example.com"));
    assertThat(user.getUsername(), is("newuser"));
    assertThat(user.getPassword(), is("newpass"));
    assertThat(user.getBio(), is("new bio"));
    assertThat(user.getImage(), is("http://new.image"));
  }

  @Test
  public void should_not_update_fields_when_empty_string_provided() {
    User user = new User("test@example.com", "testuser", "password", "bio", "image");
    user.update("", "", "", "", "");
    
    assertThat(user.getEmail(), is("test@example.com"));
    assertThat(user.getUsername(), is("testuser"));
    assertThat(user.getPassword(), is("password"));
    assertThat(user.getBio(), is("bio"));
    assertThat(user.getImage(), is("image"));
  }

  @Test
  public void should_create_user_with_null_bio_and_image() {
    User user = new User("test@example.com", "testuser", "password", null, null);
    
    assertThat(user.getId(), notNullValue());
    assertThat(user.getEmail(), is("test@example.com"));
    assertThat(user.getBio(), is((String) null));
    assertThat(user.getImage(), is((String) null));
  }

  @Test
  public void should_have_equality_based_on_id() {
    User user1 = new User("test@example.com", "testuser", "password", "bio", "image");
    User user2 = new User("test@example.com", "testuser", "password", "bio", "image");
    
    assertThat(user1.equals(user2), is(false));
    assertThat(user1.equals(user1), is(true));
  }
}
